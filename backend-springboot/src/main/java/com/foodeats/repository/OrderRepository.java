package com.foodeats.repository;

import com.foodeats.model.Order;
import com.foodeats.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId ORDER BY o.createdAt DESC")
    List<Order> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") Long customerId);

    @Query("SELECT o FROM Order o WHERE o.merchant.id = :merchantId ORDER BY o.createdAt DESC")
    List<Order> findByMerchantIdOrderByCreatedAtDesc(@Param("merchantId") Long merchantId);

    @Query("SELECT o FROM Order o WHERE o.driver.id = :driverId ORDER BY o.createdAt DESC")
    List<Order> findByDriverIdOrderByCreatedAtDesc(@Param("driverId") Long driverId);

    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<Order> findByStatusInOrderByCreatedAtDesc(List<OrderStatus> statuses);
}
