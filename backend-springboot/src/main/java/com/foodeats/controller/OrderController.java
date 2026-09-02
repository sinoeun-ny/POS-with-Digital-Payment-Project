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
        // For simplicity, we'll pass userId to service layer
        // In a real scenario, you'd fetch the full user object
        User user = new User();
        user.setId(userId);
        return user;
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestHeader("Authorization") String authHeader, 
                                        @RequestBody CheckoutRequest request) {
        try {
            User customer = getAuthenticatedUser(authHeader);
            if (customer == null) {
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }

            Order order = orderService.placeOrder(customer.getId(), request);
            return ResponseEntity.ok(order);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getOrders(@RequestHeader("Authorization") String authHeader) {
        User user = getAuthenticatedUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
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
