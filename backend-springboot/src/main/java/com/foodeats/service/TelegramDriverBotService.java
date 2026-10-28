package com.foodeats.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodeats.dto.telegram.*;
import com.foodeats.model.*;
import com.foodeats.repository.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TelegramDriverBotService {

    private static final Logger log = LoggerFactory.getLogger(TelegramDriverBotService.class);

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.bot.username:}")
    private String botUsername;

    @Value("${telegram.default.driver.chat.id:}")
    private String defaultDriverChatId;

    @Value("${telegram.webhook.url:}")
    private String webhookUrl;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final DeliveryRepository deliveryRepository;
    private final DriverLocationService driverLocationService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private ScheduledExecutorService pollingExecutor;
    private long lastUpdateId = 0;

    public TelegramDriverBotService(OrderRepository orderRepository,
                                    UserRepository userRepository,
                                    NotificationRepository notificationRepository,
                                    DeliveryRepository deliveryRepository,
                                    DriverLocationService driverLocationService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.deliveryRepository = deliveryRepository;
        this.driverLocationService = driverLocationService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void initLongPolling() {
        if (!isConfigured()) {
            log.info("Telegram Bot token not configured. Dispatch service in dormant mode.");
            return;
        }

        // When no public webhook URL is configured (local dev/testing), start active Long Poller daemon
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            log.info("Starting Telegram Long Poller daemon for @{}...", botUsername);
            pollingExecutor = Executors.newSingleThreadScheduledExecutor();
            pollingExecutor.scheduleWithFixedDelay(this::pollUpdates, 1, 1, TimeUnit.SECONDS);
        } else {
            log.info("Telegram Webhook mode active: {}", webhookUrl);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (pollingExecutor != null && !pollingExecutor.isShutdown()) {
            pollingExecutor.shutdownNow();
        }
    }

    /**
     * Long polling loop for local development without public webhook URLs
     */
    public void pollUpdates() {
        String baseUrl = getApiBaseUrl();
        if (baseUrl == null) return;

        try {
            String url = baseUrl + "/getUpdates?offset=" + (lastUpdateId + 1) + "&timeout=1";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.path("ok").asBoolean()) {
                    JsonNode result = root.path("result");
                    if (result.isArray()) {
                        for (JsonNode updateNode : result) {
                            long updateId = updateNode.path("update_id").asLong();
                            if (updateId > lastUpdateId) {
                                lastUpdateId = updateId;
                            }
                            TelegramUpdate update = objectMapper.treeToValue(updateNode, TelegramUpdate.class);
                            processWebhookUpdate(update);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Polling update check: {}", e.getMessage());
        }
    }

    private String getApiBaseUrl() {
        if (botToken == null || botToken.trim().isEmpty() || "YOUR_TELEGRAM_BOT_TOKEN".equals(botToken)) {
            return null;
        }
        return "https://api.telegram.org/bot" + botToken.trim();
    }

    public boolean isConfigured() {
        return getApiBaseUrl() != null;
    }

    public String getBotUsername() {
        return botUsername;
    }

    /**
     * Dispatches an order alert to a specific Telegram chat ID with navigation links and Accept/Reject buttons
     */
    public boolean dispatchOrderAlert(Object driverChatId, Order order) {
        String baseUrl = getApiBaseUrl();
        if (baseUrl == null) {
            log.warn("Telegram bot token not configured. Skipping Telegram dispatch for order #{}", order.getOrderNumber());
            return false;
        }

        if (driverChatId == null || driverChatId.toString().trim().isEmpty()) {
            if (defaultDriverChatId != null && !defaultDriverChatId.trim().isEmpty()) {
                driverChatId = defaultDriverChatId.trim();
            } else {
                log.warn("No Telegram driver chat ID provided for dispatching order #{}", order.getOrderNumber());
                return false;
            }
        }

        String merchantName = order.getMerchantName() != null ? order.getMerchantName() : "Restaurant Partner";
        String merchantAddress = (order.getMerchant() != null && order.getMerchant().getAddress() != null)
                ? order.getMerchant().getAddress()
                : "Phnom Penh City Center";
        String customerAddress = order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Customer Address";
        Double deliveryFee = order.getDeliveryFee() != null ? order.getDeliveryFee() : 1.50;
        Double totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : 0.00;

        String text = String.format(
                "🚨 <b>NEW ORDER DISPATCH ALERT</b>\n" +
                "━━━━━━━━━━━━━━━━━━━━━━\n" +
                "📦 <b>Order ID:</b> <code>#%s</code>\n" +
                "🏪 <b>Merchant:</b> %s\n" +
                "📍 <b>Pickup:</b> %s\n" +
                "🏁 <b>Dropoff:</b> %s\n" +
                "━━━━━━━━━━━━━━━━━━━━━━\n" +
                "💵 <b>Driver Fee:</b> $%.2f\n" +
                "🧾 <b>Order Total:</b> $%.2f\n" +
                "⏰ <b>Estimated Time:</b> 20 - 30 mins\n\n" +
                "Tap <b>Accept</b> below to claim this delivery job.",
                order.getOrderNumber(),
                escapeHtml(merchantName),
                escapeHtml(merchantAddress),
                escapeHtml(customerAddress),
                deliveryFee,
                totalAmount
        );

        // Build Google Maps Navigation URL
        String mapsUrl = "https://www.google.com/maps/dir/?api=1&destination="
                + URLEncoder.encode(merchantAddress + ", Phnom Penh, Cambodia", StandardCharsets.UTF_8);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        // Row 1: Direct Turn-by-Turn Navigation Link
        markup.addRow(List.of(
                InlineKeyboardButton.urlButton("🗺️ Open Pickup Navigation", mapsUrl)
        ));

        // Row 2: Driver Response Actions
        markup.addRow(List.of(
                InlineKeyboardButton.callbackButton("✅ Accept", "accept_" + order.getId()),
                InlineKeyboardButton.callbackButton("❌ Reject", "reject_" + order.getId())
        ));

        SendMessageRequest request = new SendMessageRequest(driverChatId, text, "HTML", markup);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SendMessageRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/sendMessage", entity, String.class);
            log.info("Telegram dispatch alert sent for order #{} to chat {}. Status: {}",
                    order.getOrderNumber(), driverChatId, response.getStatusCode());
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Failed to send Telegram dispatch alert for order #{}: {}", order.getOrderNumber(), e.getMessage());
            return false;
        }
    }

    /**
     * Broadcasts an order alert to all registered drivers with Telegram chat IDs, or default chat ID
     */
    public int broadcastOrderAlert(Order order) {
        List<User> driversWithTelegram = userRepository.findByRoleAndTelegramChatIdIsNotNull(UserRole.DRIVER);
        int sentCount = 0;

        if (!driversWithTelegram.isEmpty()) {
            for (User driver : driversWithTelegram) {
                if (dispatchOrderAlert(driver.getTelegramChatId(), order)) {
                    sentCount++;
                }
            }
        } else if (defaultDriverChatId != null && !defaultDriverChatId.trim().isEmpty()) {
            if (dispatchOrderAlert(defaultDriverChatId.trim(), order)) {
                sentCount++;
            }
        }

        return sentCount;
    }

    /**
     * Processes incoming updates from Telegram (button clicks, live location updates, text commands)
     */
    public void processWebhookUpdate(TelegramUpdate update) {
        if (update == null) return;

        try {
            // 1. Process Live GPS Updates (Telegram sends edited_message when driver moves during live share)
            if (update.getEditedMessage() != null && update.getEditedMessage().getLocation() != null) {
                handleLiveGpsUpdate(update.getEditedMessage(), true);
                return;
            }

            // 2. Process Initial Static or Live Location Share
            if (update.getMessage() != null && update.getMessage().getLocation() != null) {
                handleLiveGpsUpdate(update.getMessage(), false);
                return;
            }

            // 3. Process Driver Button Clicks (Accept / Reject callbacks)
            if (update.getCallbackQuery() != null) {
                handleCallbackQuery(update.getCallbackQuery());
                return;
            }

            // 4. Process Text Messages (Commands like /start, /orders, /help)
            if (update.getMessage() != null && update.getMessage().getText() != null) {
                handleTextMessage(update.getMessage());
            }
        } catch (Exception e) {
            log.error("Error processing Telegram update: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles Accept and Reject button clicks from driver
     */
    private void handleCallbackQuery(TelegramCallbackQuery callbackQuery) {
        String baseUrl = getApiBaseUrl();
        if (baseUrl == null) return;

        String data = callbackQuery.getData();
        if (data == null) return;

        TelegramMessage originalMsg = callbackQuery.getMessage();
        if (originalMsg == null || originalMsg.getChat() == null) return;

        Long chatId = originalMsg.getChat().getId();
        Long messageId = originalMsg.getMessageId();
        String driverName = callbackQuery.getFrom() != null && callbackQuery.getFrom().getFirstName() != null
                ? callbackQuery.getFrom().getFirstName()
                : "Driver";

        // 1. Immediately acknowledge callback query to dismiss loading state in Telegram client
        answerCallbackQuery(callbackQuery.getId(), "Processing your request...", false);

        if (data.startsWith("accept_")) {
            Long orderId = parseOrderId(data, "accept_");
            if (orderId == null) return;

            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                editMessageText(chatId, messageId, "⚠️ <b>Order not found or no longer available.</b>", null);
                return;
            }

            Order order = orderOpt.get();

            // Find driver by Telegram Chat ID or link to existing driver account
            User assignedDriver = userRepository.findByTelegramChatId(chatId).orElse(null);
            if (assignedDriver == null) {
                List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
                if (!drivers.isEmpty()) {
                    assignedDriver = drivers.get(0);
                    assignedDriver.setTelegramChatId(chatId);
                    userRepository.save(assignedDriver);
                }
            }

            if (assignedDriver != null) {
                order.setDriver(assignedDriver);
                driverName = assignedDriver.getName();
            }

            order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
            Order savedOrder = orderRepository.save(order);

            // Record or update Delivery tracking entity
            Delivery delivery = new Delivery(savedOrder, assignedDriver, "OUT_FOR_DELIVERY");
            delivery.setPickupTime(LocalDateTime.now());
            deliveryRepository.save(delivery);

            // Notify Customer in Database
            if (savedOrder.getCustomer() != null) {
                notificationRepository.save(new Notification(
                        savedOrder.getCustomer(),
                        "Driver Assigned!",
                        driverName + " accepted your order #" + savedOrder.getOrderNumber() + " and is heading to the merchant."
                ));
            }

            // Edit message to provide Pickup Navigation and "Pickup Item" button
            String merchantName = savedOrder.getMerchantName() != null ? savedOrder.getMerchantName() : "Restaurant Partner";
            String merchantAddress = (savedOrder.getMerchant() != null && savedOrder.getMerchant().getAddress() != null)
                    ? savedOrder.getMerchant().getAddress()
                    : "Phnom Penh City Center";
            String customerAddress = (savedOrder.getDeliveryAddress() != null && !savedOrder.getDeliveryAddress().isBlank())
                    ? savedOrder.getDeliveryAddress()
                    : "Customer Address";

            String confirmationText = String.format(
                    "✅ <b>Order #%s ACCEPTED</b>\n" +
                    "Assigned Driver: <b>%s</b>\n" +
                    "Status: 🛵 <b>HEADING TO MERCHANT</b>\n\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "🏪 <b>Merchant:</b> %s\n" +
                    "📍 <b>Pickup Address:</b>\n" +
                    "%s\n\n" +
                    "🏁 <b>Customer Dropoff:</b>\n" +
                    "%s\n\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "1️⃣ Tap <b>🗺️ Open Pickup Navigation</b> to reach the restaurant.\n" +
                    "2️⃣ When you collect the food, tap <b>📦 Pickup Item</b> below:",
                    savedOrder.getOrderNumber(),
                    escapeHtml(driverName),
                    escapeHtml(merchantName),
                    escapeHtml(merchantAddress),
                    escapeHtml(customerAddress)
            );

            // Build Google Maps Navigation URL to merchant pickup
            String pickupMapsUrl = "https://www.google.com/maps/dir/?api=1&destination="
                    + URLEncoder.encode(merchantAddress + ", Phnom Penh, Cambodia", StandardCharsets.UTF_8);

            InlineKeyboardMarkup pickupMarkup = new InlineKeyboardMarkup();
            pickupMarkup.addRow(List.of(
                    InlineKeyboardButton.urlButton("🗺️ Open Pickup Navigation", pickupMapsUrl)
            ));
            pickupMarkup.addRow(List.of(
                    InlineKeyboardButton.callbackButton("📦 Pickup Item", "pickup_" + savedOrder.getId())
            ));

            editMessageText(chatId, messageId, confirmationText, pickupMarkup);
            log.info("Driver {} accepted order #{} and heading to pickup", driverName, savedOrder.getOrderNumber());

        } else if (data.startsWith("pickup_")) {
            Long orderId = parseOrderId(data, "pickup_");
            if (orderId == null) return;

            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                editMessageText(chatId, messageId, "⚠️ <b>Order not found or no longer available.</b>", null);
                return;
            }

            Order order = orderOpt.get();

            // Record pickup time and update delivery status
            deliveryRepository.findByOrderId(order.getId()).ifPresent(del -> {
                del.setStatus("PICKED_UP");
                del.setPickupTime(LocalDateTime.now());
                deliveryRepository.save(del);
            });

            order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
            Order savedOrder = orderRepository.save(order);

            // Notify Customer in Database
            if (savedOrder.getCustomer() != null) {
                notificationRepository.save(new Notification(
                        savedOrder.getCustomer(),
                        "Order Picked Up! 🛵",
                        driverName + " has picked up your order #" + savedOrder.getOrderNumber() + " and is heading to your delivery address."
                ));
            }

            String customerAddress = (savedOrder.getDeliveryAddress() != null && !savedOrder.getDeliveryAddress().isBlank())
                    ? savedOrder.getDeliveryAddress()
                    : "Customer Address";

            // Edit message to provide Dropoff Navigation and "Delivered" button
            String inTransitText = String.format(
                    "📦 <b>Order #%s PICKED UP</b>\n" +
                    "Assigned Driver: <b>%s</b>\n" +
                    "Status: 🟢 <b>OUT FOR DELIVERY</b>\n\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "📍 <b>Customer Dropoff Address:</b>\n" +
                    "%s\n\n" +
                    "📡 <b>LIVE LOCATION TIP:</b>\n" +
                    "Tap 📎 -> 📍 Location -> <i>Share My Live Location</i> so customer tracks your route in real time.\n\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "1️⃣ Tap <b>🗺️ Open Dropoff Navigation</b> to reach the customer.\n" +
                    "2️⃣ When food is successfully delivered, tap <b>🏁 Delivered</b> below:",
                    savedOrder.getOrderNumber(),
                    escapeHtml(driverName),
                    escapeHtml(customerAddress)
            );

            // Build Google Maps Navigation URL to customer dropoff
            String dropoffMapsUrl = "https://www.google.com/maps/dir/?api=1&destination="
                    + URLEncoder.encode(customerAddress + ", Phnom Penh, Cambodia", StandardCharsets.UTF_8);

            InlineKeyboardMarkup dropoffMarkup = new InlineKeyboardMarkup();
            dropoffMarkup.addRow(List.of(
                    InlineKeyboardButton.urlButton("🗺️ Open Dropoff Navigation", dropoffMapsUrl)
            ));
            dropoffMarkup.addRow(List.of(
                    InlineKeyboardButton.callbackButton("🏁 Delivered", "delivered_" + savedOrder.getId())
            ));

            editMessageText(chatId, messageId, inTransitText, dropoffMarkup);
            log.info("Driver {} picked up order #{} and is heading to dropoff", driverName, savedOrder.getOrderNumber());

        } else if (data.startsWith("delivered_")) {
            Long orderId = parseOrderId(data, "delivered_");
            if (orderId == null) return;

            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                editMessageText(chatId, messageId, "⚠️ <b>Order not found or no longer available.</b>", null);
                return;
            }

            Order order = orderOpt.get();

            if (order.getStatus() == OrderStatus.DELIVERED) {
                answerCallbackQuery(callbackQuery.getId(), "Order is already marked as delivered!", false);
                return;
            }

            // Update order status to DELIVERED
            order.setStatus(OrderStatus.DELIVERED);
            Order savedOrder = orderRepository.save(order);

            // Update Delivery tracking entity
            deliveryRepository.findByOrderId(savedOrder.getId()).ifPresent(del -> {
                del.setStatus("DELIVERED");
                del.setDeliveredTime(LocalDateTime.now());
                deliveryRepository.save(del);
            });

            // Notify Customer in Database
            if (savedOrder.getCustomer() != null) {
                notificationRepository.save(new Notification(
                        savedOrder.getCustomer(),
                        "Order Delivered! 🎉",
                        "Your order #" + savedOrder.getOrderNumber() + " has been successfully delivered by " + driverName + ". Enjoy your meal!"
                ));
            }

            Double driverFee = savedOrder.getDeliveryFee() != null ? savedOrder.getDeliveryFee() : 1.50;
            Double totalAmount = savedOrder.getTotalAmount() != null ? savedOrder.getTotalAmount() : 0.00;

            String deliveredText = String.format(
                    "🎉 <b>ORDER DELIVERED SUCCESSFULLY!</b>\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "📦 <b>Order ID:</b> <code>#%s</code>\n" +
                    "👤 <b>Driver:</b> %s\n" +
                    "🏁 <b>Destination:</b> %s\n" +
                    "Status: ✅ <b>DELIVERED</b>\n\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "💵 <b>Driver Fee Earned:</b> $%.2f\n" +
                    "🧾 <b>Order Total:</b> $%.2f\n\n" +
                    "🌟 <i>Great job! Delivery marked as complete. You are ready for the next order.</i>",
                    savedOrder.getOrderNumber(),
                    escapeHtml(driverName),
                    escapeHtml(savedOrder.getDeliveryAddress() != null ? savedOrder.getDeliveryAddress() : "Customer Address"),
                    driverFee,
                    totalAmount
            );

            // Edit message to remove Delivered button and display success summary
            editMessageText(chatId, messageId, deliveredText, null);
            log.info("Driver {} successfully delivered order #{}", driverName, savedOrder.getOrderNumber());

        } else if (data.startsWith("reject_")) {
            Long orderId = parseOrderId(data, "reject_");
            String rejectedText = String.format(
                    "❌ <b>Order #%s REJECTED</b>\n" +
                    "Order returned to dispatch pool for other drivers.",
                    orderId != null ? orderId : ""
            );
            editMessageText(chatId, messageId, rejectedText, null);
            log.info("Driver rejected order ID {}", orderId);
        }
    }

    /**
     * Handles moving driver GPS updates from Telegram Live Location stream
     */
    private void handleLiveGpsUpdate(TelegramMessage message, boolean isEdit) {
        TelegramLocation loc = message.getLocation();
        if (loc == null || loc.getLatitude() == null || loc.getLongitude() == null) return;

        Long chatId = message.getChatId();
        if (chatId == null) return;

        Double lat = loc.getLatitude();
        Double lng = loc.getLongitude();
        Integer heading = loc.getHeading();
        Double accuracy = loc.getHorizontalAccuracy();
        boolean isLive = loc.getLivePeriod() != null && loc.getLivePeriod() > 0;

        // Locate driver associated with this Chat ID
        User driver = userRepository.findByTelegramChatId(chatId).orElse(null);
        if (driver == null) {
            List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
            if (!drivers.isEmpty()) {
                driver = drivers.get(0);
                driver.setTelegramChatId(chatId);
                userRepository.save(driver);
            } else {
                log.warn("Live location received from unregistered chat ID: {}", chatId);
                return;
            }
        }

        // Find active order assigned to this driver
        Long orderId = null;
        List<Order> activeOrders = orderRepository.findByDriverIdAndStatusInOrderByCreatedAtDesc(
                driver.getId(),
                List.of(OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP, OrderStatus.OUT_FOR_DELIVERY)
        );
        if (!activeOrders.isEmpty()) {
            orderId = activeOrders.get(0).getId();
        }

        // Cache and persist live GPS coordinates
        driverLocationService.updateLocation(driver.getId(), orderId, lat, lng, heading, accuracy, isLive);

        log.info("🛰️ Driver [{}] Live GPS Updated -> Lat: {}, Lng: {}, Heading: {}°, Live: {}",
                driver.getName(), lat, lng, heading != null ? heading : 0, isLive);
    }

    /**
     * Handles driver text commands like /start, /orders, /help
     */
    private void handleTextMessage(TelegramMessage message) {
        String text = message.getText().trim();
        Long chatId = message.getChatId();
        if (chatId == null) return;

        if (text.startsWith("/start")) {
            // Optional deep linking: /start driver_3
            String[] parts = text.split(" ");
            User driver = null;
            if (parts.length > 1 && parts[1].startsWith("driver_")) {
                try {
                    Long driverId = Long.parseLong(parts[1].substring("driver_".length()));
                    driver = userRepository.findById(driverId).orElse(null);
                } catch (Exception ignored) {}
            }

            if (driver == null) {
                driver = userRepository.findByTelegramChatId(chatId).orElse(null);
            }

            if (driver == null) {
                List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
                if (!drivers.isEmpty()) {
                    driver = drivers.get(0);
                }
            }

            if (driver != null) {
                driver.setTelegramChatId(chatId);
                userRepository.save(driver);
            }

            String driverName = driver != null ? driver.getName() : "Delivery Partner";
            if (message.getFrom() != null && message.getFrom().getFirstName() != null) {
                driverName = message.getFrom().getFirstName();
            }

            String welcomeText = String.format(
                    "🛵 <b>SMART FOOD DRIVER CONSOLE</b>\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "Welcome back, <b>%s</b>!\n\n" +
                    "✅ <b>Telegram Dispatch Connected</b>\n" +
                    "🆔 <b>Your Chat ID:</b> <code>%d</code>\n" +
                    "🟢 <b>Status:</b> Ready for incoming order dispatches.\n\n" +
                    "Commands:\n" +
                    "• /orders - Check available delivery orders\n" +
                    "• /status - Check your active delivery assignment\n" +
                    "• /help - View driver console instructions\n\n" +
                    "When an order is dispatched to you, you will receive an alert with turn-by-turn navigation and Accept/Reject buttons.",
                    escapeHtml(driverName),
                    chatId
            );

            sendMessage(chatId, welcomeText);

        } else if (text.equalsIgnoreCase("/orders")) {
            List<Order> availableOrders = orderRepository.findByStatusInOrderByCreatedAtDesc(
                    List.of(OrderStatus.ACCEPTED, OrderStatus.READY_FOR_PICKUP)
            );

            if (availableOrders.isEmpty()) {
                sendMessage(chatId, "ℹ️ <b>No pending orders currently available for dispatch.</b>");
            } else {
                for (Order o : availableOrders) {
                    dispatchOrderAlert(chatId, o);
                }
            }

        } else if (text.equalsIgnoreCase("/status")) {
            User driver = userRepository.findByTelegramChatId(chatId).orElse(null);
            if (driver == null) {
                sendMessage(chatId, "⚠️ Driver profile not linked yet. Type <code>/start</code> to link your console.");
                return;
            }

            List<Order> active = orderRepository.findByDriverIdAndStatusInOrderByCreatedAtDesc(
                    driver.getId(),
                    List.of(OrderStatus.OUT_FOR_DELIVERY)
            );

            if (active.isEmpty()) {
                sendMessage(chatId, "🟢 <b>You have no active orders in transit.</b>\nWaiting for next dispatch...");
            } else {
                Order cur = active.get(0);
                Delivery del = deliveryRepository.findByOrderId(cur.getId()).orElse(null);
                boolean isPickedUp = del != null && "PICKED_UP".equalsIgnoreCase(del.getStatus());

                String merchantAddress = (cur.getMerchant() != null && cur.getMerchant().getAddress() != null)
                        ? cur.getMerchant().getAddress()
                        : "Phnom Penh City Center";
                String customerAddress = (cur.getDeliveryAddress() != null && !cur.getDeliveryAddress().isBlank())
                        ? cur.getDeliveryAddress()
                        : "Phnom Penh City";

                if (isPickedUp) {
                    // Stage 2: Food picked up, heading to customer dropoff
                    String msg = String.format(
                            "🚚 <b>ACTIVE DELIVERY IN PROGRESS</b>\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "📦 <b>Order:</b> <code>#%s</code>\n" +
                            "🏁 <b>Destination:</b> %s\n" +
                            "Status: 🟢 <b>OUT FOR DELIVERY</b>\n\n" +
                            "Tap <b>Delivered</b> once you have handed the food to the customer:",
                            cur.getOrderNumber(),
                            escapeHtml(customerAddress)
                    );

                    String dropoffMapsUrl = "https://www.google.com/maps/dir/?api=1&destination="
                            + URLEncoder.encode(customerAddress + ", Phnom Penh, Cambodia", StandardCharsets.UTF_8);

                    InlineKeyboardMarkup statusMarkup = new InlineKeyboardMarkup();
                    statusMarkup.addRow(List.of(
                            InlineKeyboardButton.urlButton("🗺️ Open Dropoff Navigation", dropoffMapsUrl)
                    ));
                    statusMarkup.addRow(List.of(
                            InlineKeyboardButton.callbackButton("🏁 Delivered", "delivered_" + cur.getId())
                    ));

                    sendMessage(chatId, msg, statusMarkup);
                } else {
                    // Stage 1: Heading to merchant for food pickup
                    String msg = String.format(
                            "🛵 <b>HEADING TO MERCHANT FOR PICKUP</b>\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "📦 <b>Order:</b> <code>#%s</code>\n" +
                            "🏪 <b>Merchant:</b> %s\n" +
                            "📍 <b>Pickup Address:</b> %s\n\n" +
                            "Tap <b>Pickup Item</b> once you collect the food:",
                            cur.getOrderNumber(),
                            escapeHtml(cur.getMerchantName() != null ? cur.getMerchantName() : "Restaurant Partner"),
                            escapeHtml(merchantAddress)
                    );

                    String pickupMapsUrl = "https://www.google.com/maps/dir/?api=1&destination="
                            + URLEncoder.encode(merchantAddress + ", Phnom Penh, Cambodia", StandardCharsets.UTF_8);

                    InlineKeyboardMarkup statusMarkup = new InlineKeyboardMarkup();
                    statusMarkup.addRow(List.of(
                            InlineKeyboardButton.urlButton("🗺️ Open Pickup Navigation", pickupMapsUrl)
                    ));
                    statusMarkup.addRow(List.of(
                            InlineKeyboardButton.callbackButton("📦 Pickup Item", "pickup_" + cur.getId())
                    ));

                    sendMessage(chatId, msg, statusMarkup);
                }
            }

        } else if (text.equalsIgnoreCase("/help")) {
            String helpText =
                    "📚 <b>DRIVER CONSOLE HELP</b>\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "1. When an alert arrives, tap <b>🗺️ Open Pickup Navigation</b> to open Google Maps.\n" +
                    "2. Tap <b>✅ Accept</b> to claim the order.\n" +
                    "3. Tap 📎 -> 📍 Location -> <b>Share My Live Location</b> so customer and restaurant track your arrival in real time.\n" +
                    "4. Type <code>/status</code> anytime to view active delivery information.";
            sendMessage(chatId, helpText);
        }
    }

    public boolean sendMessage(Object chatId, String text) {
        return sendMessage(chatId, text, null);
    }

    public boolean sendMessage(Object chatId, String text, InlineKeyboardMarkup markup) {
        String baseUrl = getApiBaseUrl();
        if (baseUrl == null) return false;

        SendMessageRequest request = new SendMessageRequest(chatId, text, "HTML", markup);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SendMessageRequest> entity = new HttpEntity<>(request, headers);
            restTemplate.postForEntity(baseUrl + "/sendMessage", entity, String.class);
            return true;
        } catch (Exception e) {
            log.error("Failed to send Telegram message: {}", e.getMessage());
            return false;
        }
    }

    public boolean editMessageText(Object chatId, Long messageId, String text, InlineKeyboardMarkup markup) {
        String baseUrl = getApiBaseUrl();
        if (baseUrl == null) return false;

        EditMessageTextRequest request = new EditMessageTextRequest(chatId, messageId, text, "HTML", markup);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<EditMessageTextRequest> entity = new HttpEntity<>(request, headers);
            restTemplate.postForEntity(baseUrl + "/editMessageText", entity, String.class);
            return true;
        } catch (Exception e) {
            log.error("Failed to edit Telegram message: {}", e.getMessage());
            return false;
        }
    }

    public boolean answerCallbackQuery(String callbackQueryId, String text, boolean showAlert) {
        String baseUrl = getApiBaseUrl();
        if (baseUrl == null) return false;

        AnswerCallbackQueryRequest request = new AnswerCallbackQueryRequest(callbackQueryId, text, showAlert);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AnswerCallbackQueryRequest> entity = new HttpEntity<>(request, headers);
            restTemplate.postForEntity(baseUrl + "/answerCallbackQuery", entity, String.class);
            return true;
        } catch (Exception e) {
            log.error("Failed to answer Telegram callback query: {}", e.getMessage());
            return false;
        }
    }

    public Map<String, Object> setWebhook(String webhookUrl) {
        String baseUrl = getApiBaseUrl();
        if (baseUrl == null) return Map.of("success", false, "message", "Bot token not configured");

        try {
            String url = baseUrl + "/setWebhook?url=" + URLEncoder.encode(webhookUrl, StandardCharsets.UTF_8);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody() != null ? response.getBody() : Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    public Map<String, Object> getWebhookInfo() {
        String baseUrl = getApiBaseUrl();
        if (baseUrl == null) return Map.of("success", false, "message", "Bot token not configured");

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl + "/getWebhookInfo", Map.class);
            return response.getBody() != null ? response.getBody() : Collections.emptyMap();
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    public Map<String, Object> deleteWebhook() {
        String baseUrl = getApiBaseUrl();
        if (baseUrl == null) return Map.of("success", false, "message", "Bot token not configured");

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl + "/deleteWebhook", Map.class);
            return response.getBody() != null ? response.getBody() : Collections.emptyMap();
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    private Long parseOrderId(String data, String prefix) {
        try {
            return Long.parseLong(data.substring(prefix.length()));
        } catch (Exception e) {
            return null;
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }
}
