package com.foodeats.repository;

import com.foodeats.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    @Query("SELECT d FROM Delivery d WHERE d.order.id = :orderId")
    Optional<Delivery> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT d FROM Delivery d WHERE d.driver.id = :driverId")
    List<Delivery> findByDriverId(@Param("driverId") Long driverId);
}
