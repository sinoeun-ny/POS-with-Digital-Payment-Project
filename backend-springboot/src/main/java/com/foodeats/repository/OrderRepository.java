package com.foodeats.repository;

import com.foodeats.model.Order;
import com.foodeats.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Order> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);
    List<Order> findByDriverIdOrderByCreatedAtDesc(Long driverId);
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
}
