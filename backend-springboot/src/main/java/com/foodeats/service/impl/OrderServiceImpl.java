package com.foodeats.service.impl;

import com.foodeats.dto.CheckoutRequest;
import com.foodeats.model.*;
import com.foodeats.repository.*;
import com.foodeats.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                           CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           MenuItemRepository menuItemRepository,
                           MerchantRepository merchantRepository,
                           UserRepository userRepository,
                           PaymentRepository paymentRepository,
                           NotificationRepository notificationRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.menuItemRepository = menuItemRepository;
        this.merchantRepository = merchantRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public Order placeOrder(Long customerId, CheckoutRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found"));

        Optional<Cart> cartOpt = cartRepository.findByCustomerId(customerId);
        Cart cart = cartOpt.orElse(null);
        List<CartItem> cartItems = (cart != null) ? cart.getItems() : null;

        double subtotal = 0.0;
        Order order = new Order();
        order.setCustomer(customer);
        order.setMerchant(merchant);
        order.setMerchantName(merchant.getName());
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setDeliveryFee(merchant.getDeliveryFee());
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress(request.getDeliveryAddress() != null 
                ? request.getDeliveryAddress() : "Phnom Penh City");
        order.setPaymentStatus("PAID");
        order.setCreatedAt(LocalDateTime.now());

        if (cartItems != null && !cartItems.isEmpty()) {
            subtotal = cartItems.stream()
                    .mapToDouble(item -> item.getMenuItem().getPrice() * item.getQuantity())
                    .sum();
            for (CartItem ci : cartItems) {
                OrderItem oi = new OrderItem(order, ci.getMenuItem(), ci.getQuantity(), ci.getMenuItem().getPrice());
                order.getItems().add(oi);
            }
            cartItemRepository.deleteByCartId(cart.getId());
        } else if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (CheckoutRequest.OrderItemDto itemDto : request.getItems()) {
                MenuItem mi = null;
                if (itemDto.getMenuItemId() != null) {
                    mi = menuItemRepository.findById(itemDto.getMenuItemId()).orElse(null);
                }
                double price = (itemDto.getPrice() != null && itemDto.getPrice() > 0) 
                        ? itemDto.getPrice() 
                        : (mi != null ? mi.getPrice() : 5.0);
                int qty = (itemDto.getQuantity() != null && itemDto.getQuantity() > 0) 
                        ? itemDto.getQuantity() : 1;
                subtotal += price * qty;
                OrderItem oi = new OrderItem(order, mi, qty, price, itemDto.getSelectedOptions());
                if (itemDto.getItemName() != null && !itemDto.getItemName().isBlank()) {
                    oi.setMenuItemName(itemDto.getItemName());
                } else if (mi != null) {
                    oi.setMenuItemName(mi.getName());
                } else {
                    oi.setMenuItemName("Chef Special");
                }
                order.getItems().add(oi);
            }
        } else {
            // Direct order fallback (from mobile app or web checkout):
            List<MenuItem> merchantMenu = menuItemRepository.findByMerchantId(merchant.getId());
            if (!merchantMenu.isEmpty()) {
                MenuItem firstItem = merchantMenu.get(0);
                subtotal = firstItem.getPrice();
                OrderItem oi = new OrderItem(order, firstItem, 1, firstItem.getPrice());
                order.getItems().add(oi);
            } else {
                subtotal = 5.00;
            }
        }

        double totalAmount = subtotal + merchant.getDeliveryFee();
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        Payment payment = new Payment(savedOrder, request.getPaymentMethod(), 
                "TXN-" + System.currentTimeMillis(), totalAmount, "SUCCESS");
        paymentRepository.save(payment);

        if (merchant.getOwner() != null) {
            notificationRepository.save(new Notification(
                merchant.getOwner(), 
                "New Order #" + savedOrder.getId(), 
                "You received a new order for $" + String.format("%.2f", totalAmount)
            ));
        }

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(Long userId, String userRole) {
        UserRole role = UserRole.valueOf(userRole);
        
        switch (role) {
            case MERCHANT:
                List<Merchant> merchants = merchantRepository.findByOwnerId(userId);
                if (!merchants.isEmpty()) {
                    List<Long> mIds = merchants.stream().map(Merchant::getId).toList();
                    return orderRepository.findAll().stream()
                            .filter(o -> o.getMerchant() != null && mIds.contains(o.getMerchant().getId()))
                            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                            .toList();
                }
                return orderRepository.findAll();
            case DRIVER:
                return orderRepository.findByDriverIdOrderByCreatedAtDesc(userId);
            case ADMIN:
                return orderRepository.findAll();
            default:
                return orderRepository.findByCustomerIdOrderByCreatedAtDesc(userId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);

        notificationRepository.save(new Notification(
            order.getCustomer(), 
            "Order #" + order.getId() + " Status", 
            "Your order status is now " + newStatus.name()
        ));

        return updated;
    }

    @Override
    @Transactional
    public Order assignDriverToOrder(Long orderId, Long driverId) {
        User driver = userRepository.findById(driverId)
                .filter(u -> u.getRole() == UserRole.DRIVER)
                .orElseThrow(() -> new IllegalArgumentException("Invalid driver"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.setDriver(driver);
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        Order updated = orderRepository.save(order);

        notificationRepository.save(new Notification(
            order.getCustomer(), 
            "Driver Assigned!", 
            driver.getName() + " is delivering your order #" + order.getId()
        ));

        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAvailableDeliveryJobs() {
        return orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.ACCEPTED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByMerchantId(Long merchantId) {
        return orderRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
