package com.foodeats.repository;

import com.foodeats.model.DriverLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DriverLocationRepository extends JpaRepository<DriverLocation, Long> {
    Optional<DriverLocation> findFirstByDriverIdOrderByUpdatedAtDesc(Long driverId);
    Optional<DriverLocation> findFirstByCurrentOrderIdOrderByUpdatedAtDesc(Long orderId);
    List<DriverLocation> findByIsLiveTrue();
}
