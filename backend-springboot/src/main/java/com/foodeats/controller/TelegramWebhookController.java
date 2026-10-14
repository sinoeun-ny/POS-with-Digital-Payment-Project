package com.foodeats.controller;

import com.foodeats.dto.telegram.TelegramUpdate;
import com.foodeats.model.Order;
import com.foodeats.model.User;
import com.foodeats.model.UserRole;
import com.foodeats.repository.OrderRepository;
import com.foodeats.repository.UserRepository;
import com.foodeats.service.TelegramDriverBotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/telegram")
public class TelegramWebhookController {

    private final TelegramDriverBotService telegramBotService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public TelegramWebhookController(TelegramDriverBotService telegramBotService,
                                     OrderRepository orderRepository,
                                     UserRepository userRepository) {
        this.telegramBotService = telegramBotService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    /**
     * Webhook endpoint called by Telegram when drivers interact with the bot
     * Handles: /start, Accept/Reject buttons, and real-time Live Location streams
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> onWebhookUpdate(@RequestBody(required = false) TelegramUpdate update) {
        if (update != null) {
            telegramBotService.processWebhookUpdate(update);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Manually or automatically dispatch an order alert to a driver via Telegram
     */
    @PostMapping("/dispatch/{orderId}")
    public ResponseEntity<?> dispatchOrderToDriver(
            @PathVariable Long orderId,
            @RequestParam(required = false) Long chatId) {

        return orderRepository.findById(orderId).map(order -> {
            boolean sent = telegramBotService.dispatchOrderAlert(chatId, order);
            if (sent) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Order #" + order.getOrderNumber() + " dispatch alert sent to Telegram",
                        "orderId", order.getId()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Failed to send Telegram alert. Check bot token configuration."
                ));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Broadcasts an order alert to all online registered drivers with Telegram
     */
    @PostMapping("/broadcast/{orderId}")
    public ResponseEntity<?> broadcastOrderToDrivers(@PathVariable Long orderId) {
        return orderRepository.findById(orderId).map(order -> {
            int count = telegramBotService.broadcastOrderAlert(order);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "orderId", order.getId(),
                    "dispatchedDriversCount", count
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Check Telegram Bot connection status and registered drivers
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        Map<String, Object> status = new HashMap<>();
        boolean configured = telegramBotService.isConfigured();
        status.put("botConfigured", configured);
        status.put("botUsername", telegramBotService.getBotUsername());

        List<User> driversWithTelegram = userRepository.findByRoleAndTelegramChatIdIsNotNull(UserRole.DRIVER);
        status.put("connectedDriversCount", driversWithTelegram.size());
        status.put("connectedDrivers", driversWithTelegram.stream().map(d -> Map.of(
                "id", d.getId(),
                "name", d.getName(),
                "phone", d.getPhone(),
                "telegramChatId", d.getTelegramChatId()
        )).toList());

        if (configured) {
            try {
                status.put("telegramWebhookInfo", telegramBotService.getWebhookInfo());
            } catch (Exception ignored) {}
        }

        return ResponseEntity.ok(status);
    }

    /**
     * Links a driver user account with a Telegram Chat ID
     */
    @PostMapping("/register-driver")
    public ResponseEntity<?> registerDriverChatId(@RequestBody Map<String, Object> payload) {
        Long driverId = payload.get("driverId") != null ? Long.valueOf(payload.get("driverId").toString()) : null;
        String phone = payload.get("phone") != null ? payload.get("phone").toString() : null;
        Object rawChatId = payload.get("chatId");

        if (rawChatId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "chatId is required"));
        }

        Long chatId = Long.valueOf(rawChatId.toString());
        User driver = null;

        if (driverId != null) {
            driver = userRepository.findById(driverId).orElse(null);
        } else if (phone != null) {
            driver = userRepository.findByPhone(phone).orElse(null);
        }

        if (driver == null) {
            List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
            if (!drivers.isEmpty()) {
                driver = drivers.get(0);
            }
        }

        if (driver == null) {
            return ResponseEntity.notFound().build();
        }

        driver.setTelegramChatId(chatId);
        User saved = userRepository.save(driver);

        telegramBotService.sendMessage(chatId,
                "✅ *Account Linked!* You are registered as driver *" + saved.getName() + "*."
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "driverId", saved.getId(),
                "driverName", saved.getName(),
                "telegramChatId", saved.getTelegramChatId()
        ));
    }

    /**
     * Configure Telegram Webhook URL
     */
    @PostMapping("/set-webhook")
    public ResponseEntity<?> setWebhook(@RequestParam String url) {
        Map<String, Object> result = telegramBotService.setWebhook(url);
        return ResponseEntity.ok(result);
    }

    /**
     * Delete Telegram Webhook
     */
    @DeleteMapping("/delete-webhook")
    public ResponseEntity<?> deleteWebhook() {
        Map<String, Object> result = telegramBotService.deleteWebhook();
        return ResponseEntity.ok(result);
    }
}
