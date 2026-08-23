package com.foodeats.controller;

import com.foodeats.model.Order;
import com.foodeats.model.OrderStatus;
import com.foodeats.model.UserRole;
import com.foodeats.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final OrderRepository orderRepository;

    private static final Map<String, Object> systemSettings = new HashMap<>();

    static {
        systemSettings.put("systemName", "Smart FoodEats Platform");
        systemSettings.put("platformFeePercent", 10.0);
        systemSettings.put("defaultDeliveryFee", 1.50);
        systemSettings.put("currency", "USD ($)");
        systemSettings.put("maintenanceMode", false);
        systemSettings.put("supportPhone", "+855 23 999 888");
        systemSettings.put("supportEmail", "support@foodeats.com");
        systemSettings.put("autoAssignDrivers", true);
    }

    public AdminController(UserRepository userRepository, MerchantRepository merchantRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.merchantRepository = merchantRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalMerchants = merchantRepository.count();
        long totalOrders = orderRepository.count();

        List<Order> orders = orderRepository.findAll();
        double totalRevenue = orders.stream().mapToDouble(Order::getTotalAmount).sum();

        long pendingOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long deliveredOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long outForDelivery = orders.stream().filter(o -> o.getStatus() == OrderStatus.OUT_FOR_DELIVERY).count();

        long driversCount = userRepository.findByRole(UserRole.DRIVER).size();

        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "totalMerchants", totalMerchants,
                "totalOrders", totalOrders,
                "totalRevenue", totalRevenue,
                "pendingOrders", pendingOrders,
                "deliveredOrders", deliveredOrders,
                "outForDelivery", outForDelivery,
                "driversCount", driversCount
        ));
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics() {
        List<Order> orders = orderRepository.findAll();

        Map<String, Long> statusBreakdown = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getStatus().name(), Collectors.counting()));

        Map<String, Double> merchantRevenue = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getMerchant() != null ? o.getMerchant().getName() : "Unknown Store",
                        Collectors.summingDouble(Order::getTotalAmount)
                ));

        List<Map<String, Object>> revenueTrend = List.of(
                Map.of("day", "Mon", "revenue", 120.50, "orders", 12),
                Map.of("day", "Tue", "revenue", 215.00, "orders", 18),
                Map.of("day", "Wed", "revenue", 180.25, "orders", 15),
                Map.of("day", "Thu", "revenue", 290.80, "orders", 24),
                Map.of("day", "Fri", "revenue", 410.00, "orders", 35),
                Map.of("day", "Sat", "revenue", 530.50, "orders", 42),
                Map.of("day", "Sun", "revenue", 480.00, "orders", 38)
        );

        return ResponseEntity.ok(Map.of(
                "statusBreakdown", statusBreakdown,
                "merchantRevenue", merchantRevenue,
                "revenueTrend", revenueTrend
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/merchants")
    public ResponseEntity<?> getAllMerchants() {
        return ResponseEntity.ok(merchantRepository.findAll());
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @GetMapping("/settings")
    public ResponseEntity<?> getSettings() {
        return ResponseEntity.ok(systemSettings);
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, Object> newSettings) {
        systemSettings.putAll(newSettings);
        return ResponseEntity.ok(systemSettings);
    }
}
