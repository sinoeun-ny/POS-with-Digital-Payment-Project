package com.foodeats.service;

import com.foodeats.model.DriverLocation;
import com.foodeats.model.Order;
import com.foodeats.model.User;
import com.foodeats.repository.DriverLocationRepository;
import com.foodeats.repository.OrderRepository;
import com.foodeats.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DriverLocationService {

    private final DriverLocationRepository locationRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    // Fast in-memory cache for ultra-responsive real-time tracking
    private final Map<Long, DriverLocationSnapshot> driverCache = new ConcurrentHashMap<>();
    private final Map<Long, Long> orderToDriverMap = new ConcurrentHashMap<>();

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DriverLocationService(DriverLocationRepository locationRepository,
                                 UserRepository userRepository,
                                 OrderRepository orderRepository) {
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    public static class DriverLocationSnapshot {
        private Long driverId;
        private String driverName;
        private String driverPhone;
        private Long orderId;
        private Double latitude;
        private Double longitude;
        private Integer heading;
        private Double accuracy;
        private Boolean isLive;
        private LocalDateTime updatedAt;
        private String formattedTime;

        public DriverLocationSnapshot() {}

        public DriverLocationSnapshot(Long driverId, String driverName, String driverPhone, Long orderId,
                                      Double latitude, Double longitude, Integer heading, Double accuracy,
                                      Boolean isLive, LocalDateTime updatedAt) {
            this.driverId = driverId;
            this.driverName = driverName;
            this.driverPhone = driverPhone;
            this.orderId = orderId;
            this.latitude = latitude;
            this.longitude = longitude;
            this.heading = heading;
            this.accuracy = accuracy;
            this.isLive = isLive;
            this.updatedAt = updatedAt;
            this.formattedTime = updatedAt != null ? updatedAt.format(TIME_FORMATTER) : null;
        }

        public Long getDriverId() { return driverId; }
        public String getDriverName() { return driverName; }
        public String getDriverPhone() { return driverPhone; }
        public Long getOrderId() { return orderId; }
        public Double getLatitude() { return latitude; }
        public Double getLongitude() { return longitude; }
        public Integer getHeading() { return heading; }
        public Double getAccuracy() { return accuracy; }
        public Boolean getIsLive() { return isLive; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public String getFormattedTime() { return formattedTime; }
    }

    public DriverLocationSnapshot updateLocation(Long driverId, Long orderId, Double latitude, Double longitude,
                                                 Integer heading, Double accuracy, Boolean isLive) {
        User driver = userRepository.findById(driverId).orElse(null);
        if (driver == null) return null;

        Order order = null;
        if (orderId != null) {
            order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                orderToDriverMap.put(orderId, driverId);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        DriverLocation entity = new DriverLocation(driver, order, latitude, longitude, heading, accuracy, isLive);
        entity.setUpdatedAt(now);
        locationRepository.save(entity);

        DriverLocationSnapshot snapshot = new DriverLocationSnapshot(
                driver.getId(),
                driver.getName(),
                driver.getPhone(),
                order != null ? order.getId() : null,
                latitude,
                longitude,
                heading != null ? heading : 0,
                accuracy != null ? accuracy : 5.0,
                isLive != null ? isLive : true,
                now
        );

        driverCache.put(driverId, snapshot);
        if (order != null) {
            orderToDriverMap.put(order.getId(), driverId);
        }

        return snapshot;
    }

    public Optional<DriverLocationSnapshot> getLocationByDriver(Long driverId) {
        DriverLocationSnapshot cached = driverCache.get(driverId);
        if (cached != null) {
            return Optional.of(cached);
        }

        // Fallback to DB
        return locationRepository.findFirstByDriverIdOrderByUpdatedAtDesc(driverId)
                .map(loc -> {
                    DriverLocationSnapshot snap = new DriverLocationSnapshot(
                            loc.getDriver().getId(),
                            loc.getDriver().getName(),
                            loc.getDriver().getPhone(),
                            loc.getCurrentOrder() != null ? loc.getCurrentOrder().getId() : null,
                            loc.getLatitude(),
                            loc.getLongitude(),
                            loc.getHeading(),
                            loc.getAccuracy(),
                            loc.getIsLive(),
                            loc.getUpdatedAt()
                    );
                    driverCache.put(driverId, snap);
                    return snap;
                });
    }

    public Optional<DriverLocationSnapshot> getLocationByOrder(Long orderId) {
        Long driverId = orderToDriverMap.get(orderId);
        if (driverId != null) {
            Optional<DriverLocationSnapshot> snapshot = getLocationByDriver(driverId);
            if (snapshot.isPresent()) return snapshot;
        }

        // Fallback to order assignment in DB
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent() && orderOpt.get().getDriver() != null) {
            Long assignedDriverId = orderOpt.get().getDriver().getId();
            orderToDriverMap.put(orderId, assignedDriverId);
            return getLocationByDriver(assignedDriverId);
        }

        return Optional.empty();
    }

    public Collection<DriverLocationSnapshot> getAllActiveDriverLocations() {
        return driverCache.values();
    }
}
