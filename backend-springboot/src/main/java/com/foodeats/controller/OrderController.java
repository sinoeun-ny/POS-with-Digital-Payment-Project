package com.foodeats.controller;

import com.foodeats.dto.CheckoutRequest;
import com.foodeats.model.*;
import com.foodeats.repository.*;
import com.foodeats.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final JwtUtil jwtUtil;

    public OrderController(OrderRepository orderRepository, CartRepository cartRepository,
                           CartItemRepository cartItemRepository, MerchantRepository merchantRepository,
                           UserRepository userRepository, PaymentRepository paymentRepository,
                           NotificationRepository notificationRepository, JwtUtil jwtUtil) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.merchantRepository = merchantRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.notificationRepository = notificationRepository;
        this.jwtUtil = jwtUtil;
    }

    private User getAuthenticatedUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) return null;
        return userRepository.findById(userId).orElse(null);
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestHeader("Authorization") String authHeader, @RequestBody CheckoutRequest request) {
        User customer = getAuthenticatedUser(authHeader);
        if (customer == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        Optional<Cart> cartOpt = cartRepository.findByCustomerId(customer.getId());
        if (cartOpt.isEmpty() || cartOpt.get().getItems().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Shopping cart is empty"));
        }

        Cart cart = cartOpt.get();
        Optional<Merchant> merchantOpt = merchantRepository.findById(request.getMerchantId());
        if (merchantOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "Merchant not found"));

        Merchant merchant = merchantOpt.get();

        double subtotal = cart.getItems().stream()
                .mapToDouble(item -> item.getMenuItem().getPrice() * item.getQuantity())
                .sum();

        double totalAmount = subtotal + merchant.getDeliveryFee();

        Order order = new Order();
        order.setCustomer(customer);
        order.setMerchant(merchant);
        order.setTotalAmount(totalAmount);
        order.setDeliveryFee(merchant.getDeliveryFee());
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress(request.getDeliveryAddress() != null ? request.getDeliveryAddress() : "Phnom Penh City");
        order.setPaymentStatus("PAID");

        for (CartItem ci : cart.getItems()) {
            OrderItem oi = new OrderItem(order, ci.getMenuItem(), ci.getQuantity(), ci.getMenuItem().getPrice());
            order.getItems().add(oi);
        }

        Order savedOrder = orderRepository.save(order);

        // Process Mock Payment
        Payment payment = new Payment(savedOrder, request.getPaymentMethod(), "TXN-" + System.currentTimeMillis(), totalAmount, "SUCCESS");
        paymentRepository.save(payment);

        // Notify Merchant
        if (merchant.getOwner() != null) {
            notificationRepository.save(new Notification(merchant.getOwner(), "New Order #" + savedOrder.getId(), "You received a new order for $" + String.format("%.2f", totalAmount)));
        }

        // Clear Cart
        cartItemRepository.deleteByCartId(cart.getId());

        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping
    public ResponseEntity<?> getOrders(@RequestHeader("Authorization") String authHeader) {
        User user = getAuthenticatedUser(authHeader);
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        List<Order> orders;
        if (user.getRole() == UserRole.MERCHANT) {
            Optional<Merchant> merchantOpt = merchantRepository.findByOwnerId(user.getId());
            orders = merchantOpt.map(m -> orderRepository.findByMerchantIdOrderByCreatedAtDesc(m.getId())).orElse(List.of());
        } else if (user.getRole() == UserRole.DRIVER) {
            orders = orderRepository.findByDriverIdOrderByCreatedAtDesc(user.getId());
        } else if (user.getRole() == UserRole.ADMIN) {
            orders = orderRepository.findAll();
        } else {
            orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(user.getId());
        }

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty()) return ResponseEntity.notFound().build();

        Order order = orderOpt.get();
        String newStatusStr = body.get("status");

        try {
            OrderStatus newStatus = OrderStatus.valueOf(newStatusStr.toUpperCase());
            order.setStatus(newStatus);
            Order updated = orderRepository.save(order);

            // Notify Customer
            notificationRepository.save(new Notification(order.getCustomer(), "Order #" + order.getId() + " Status", "Your order status is now " + newStatus.name()));

            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid order status"));
        }
    }
}
