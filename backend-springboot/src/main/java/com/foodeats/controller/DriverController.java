package com.foodeats.controller;

import com.foodeats.model.*;
import com.foodeats.repository.*;
import com.foodeats.security.JwtUtil;
import com.foodeats.service.DriverLocationService;
import com.foodeats.service.TelegramDriverBotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver")
public class DriverController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final DeliveryRepository deliveryRepository;
    private final DriverLocationService driverLocationService;
    private final TelegramDriverBotService telegramBotService;
    private final JwtUtil jwtUtil;

    public DriverController(OrderRepository orderRepository, UserRepository userRepository,
                            NotificationRepository notificationRepository,
                            DeliveryRepository deliveryRepository,
                            DriverLocationService driverLocationService,
                            TelegramDriverBotService telegramBotService,
                            JwtUtil jwtUtil) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.deliveryRepository = deliveryRepository;
        this.driverLocationService = driverLocationService;
        this.telegramBotService = telegramBotService;
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
        List<Order> availableOrders = orderRepository.findByStatusInOrderByCreatedAtDesc(
                List.of(OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP));
        return ResponseEntity.ok(availableOrders);
    }

    @PutMapping("/orders/{id}/accept")
    public ResponseEntity<?> acceptDeliveryJob(@RequestHeader(value = "Authorization", required = false) String authHeader, @PathVariable Long id) {
        User driver = getAuthenticatedUser(authHeader);
        if (driver == null || driver.getRole() != UserRole.DRIVER) {
            List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
            if (!drivers.isEmpty()) {
                driver = drivers.get(0);
            } else {
                return ResponseEntity.status(403).body(Map.of("message", "Driver account required"));
            }
        }

        final User assignedDriver = driver;
        return orderRepository.findById(id).map(order -> {
            order.setDriver(assignedDriver);
            order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
            Order saved = orderRepository.save(order);

            Delivery delivery = new Delivery(saved, assignedDriver, "OUT_FOR_DELIVERY");
            delivery.setPickupTime(LocalDateTime.now());
            deliveryRepository.save(delivery);

            if (order.getCustomer() != null) {
                notificationRepository.save(new Notification(order.getCustomer(), "Driver Assigned!", assignedDriver.getName() + " is delivering your order #" + order.getOrderNumber()));
            }
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateDeliveryStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return orderRepository.findById(id).map(order -> {
            String status = body.get("status");
            if (status == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Status is required"));
            }
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(newStatus);
            Order saved = orderRepository.save(order);

            deliveryRepository.findByOrderId(order.getId()).ifPresent(del -> {
                del.setStatus(newStatus.name());
                if (newStatus == OrderStatus.DELIVERED) {
                    del.setDeliveredTime(LocalDateTime.now());
                }
                deliveryRepository.save(del);
            });

            if (order.getCustomer() != null) {
                notificationRepository.save(new Notification(order.getCustomer(), "Delivery Update", "Order #" + order.getOrderNumber() + " is now " + status));
            }
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/location")
    public ResponseEntity<?> getDriverLocation(@PathVariable Long id) {
        return driverLocationService.getLocationByDriver(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/locations")
    public ResponseEntity<?> getAllDriverLocations() {
        return ResponseEntity.ok(driverLocationService.getAllActiveDriverLocations());
    }

    @PostMapping("/location/mock")
    public ResponseEntity<?> mockDriverLocation(@RequestBody Map<String, Object> body) {
        Long driverId = body.get("driverId") != null ? Long.valueOf(body.get("driverId").toString()) : 3L;
        Long orderId = body.get("orderId") != null ? Long.valueOf(body.get("orderId").toString()) : null;
        Double lat = body.get("latitude") != null ? Double.valueOf(body.get("latitude").toString()) : 11.5564;
        Double lng = body.get("longitude") != null ? Double.valueOf(body.get("longitude").toString()) : 104.9282;
        Integer heading = body.get("heading") != null ? Integer.valueOf(body.get("heading").toString()) : 90;
        Double accuracy = body.get("accuracy") != null ? Double.valueOf(body.get("accuracy").toString()) : 3.0;
        Boolean isLive = body.get("isLive") != null ? Boolean.valueOf(body.get("isLive").toString()) : true;

        DriverLocationService.DriverLocationSnapshot snapshot = driverLocationService.updateLocation(
                driverId, orderId, lat, lng, heading, accuracy, isLive
        );

        if (snapshot == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Driver not found"));
        }
        return ResponseEntity.ok(snapshot);
    }
}
