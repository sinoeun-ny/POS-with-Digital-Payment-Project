package com.foodeats.controller;

import com.foodeats.model.*;
import com.foodeats.repository.*;
import com.foodeats.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver")
public class DriverController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final JwtUtil jwtUtil;

    public DriverController(OrderRepository orderRepository, UserRepository userRepository,
                            NotificationRepository notificationRepository, JwtUtil jwtUtil) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
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

    @GetMapping("/orders")
    public ResponseEntity<?> getAvailableDeliveryJobs() {
        List<Order> availableOrders = orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.ACCEPTED);
        return ResponseEntity.ok(availableOrders);
    }

    @PutMapping("/orders/{id}/accept")
    public ResponseEntity<?> acceptDeliveryJob(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        User driver = getAuthenticatedUser(authHeader);
        if (driver == null || driver.getRole() != UserRole.DRIVER) {
            return ResponseEntity.status(403).body(Map.of("message", "Driver access required"));
        }

        return orderRepository.findById(id).map(order -> {
            order.setDriver(driver);
            order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
            Order saved = orderRepository.save(order);

            notificationRepository.save(new Notification(order.getCustomer(), "Driver Assigned!", driver.getName() + " is delivering your order #" + order.getId()));
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateDeliveryStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return orderRepository.findById(id).map(order -> {
            String status = body.get("status");
            order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
            Order saved = orderRepository.save(order);

            notificationRepository.save(new Notification(order.getCustomer(), "Delivery Update", "Order #" + order.getId() + " is now " + status));
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }
}
