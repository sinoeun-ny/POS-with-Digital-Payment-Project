package com.foodeats.controller;

import com.foodeats.dto.CheckoutRequest;
import com.foodeats.model.Order;
import com.foodeats.model.User;
import com.foodeats.security.JwtUtil;
import com.foodeats.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    public OrderController(OrderService orderService, JwtUtil jwtUtil) {
        this.orderService = orderService;
        this.jwtUtil = jwtUtil;
    }

    private User getAuthenticatedUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) return null;
        String roleStr = jwtUtil.extractRole(token);
        User user = new User();
        user.setId(userId);
        if (roleStr != null) {
            try {
                user.setRole(com.foodeats.model.UserRole.valueOf(roleStr));
            } catch (Exception ignored) {}
        }
        return user;
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestHeader(value = "Authorization", required = false) String authHeader, 
                                        @RequestBody CheckoutRequest request) {
        try {
            User customer = getAuthenticatedUser(authHeader);
            Long customerId = (customer != null && customer.getId() != null) ? customer.getId() : 4L;

            Order order = orderService.placeOrder(customerId, request);
            return ResponseEntity.ok(order);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Error placing order"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getOrders(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                       @RequestParam(value = "merchantId", required = false) Long merchantId,
                                       @RequestParam(value = "all", required = false, defaultValue = "false") boolean all) {
        if (all || (merchantId != null && merchantId <= 0)) {
            return ResponseEntity.ok(orderService.getAllOrders());
        }

        if (merchantId != null && merchantId > 0) {
            return ResponseEntity.ok(orderService.getOrdersByMerchantId(merchantId));
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(orderService.getAllOrders());
        }

        User user = getAuthenticatedUser(authHeader);
        if (user == null || user.getRole() == com.foodeats.model.UserRole.ADMIN || user.getRole() == com.foodeats.model.UserRole.MERCHANT) {
            return ResponseEntity.ok(orderService.getAllOrders());
        }

        List<Order> orders = orderService.getOrdersByUser(user.getId(), user.getRole().name());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, 
                                               @RequestBody Map<String, String> body) {
        try {
            String newStatusStr = body.get("status");
            Order updated = orderService.updateOrderStatus(id, 
                com.foodeats.model.OrderStatus.valueOf(newStatusStr.toUpperCase()));
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid order status"));
        }
    }
}
