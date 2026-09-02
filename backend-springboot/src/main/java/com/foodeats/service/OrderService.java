package com.foodeats.service;

import com.foodeats.dto.CheckoutRequest;
import com.foodeats.model.Order;
import com.foodeats.model.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public interface OrderService {
    
    @Transactional
    Order placeOrder(Long customerId, CheckoutRequest request);
    
    List<Order> getOrdersByUser(Long userId, String userRole);
    
    Order getOrderById(Long orderId);
    
    @Transactional
    Order updateOrderStatus(Long orderId, OrderStatus newStatus);
    
    @Transactional
    Order assignDriverToOrder(Long orderId, Long driverId);
    
    List<Order> getAvailableDeliveryJobs();
}
