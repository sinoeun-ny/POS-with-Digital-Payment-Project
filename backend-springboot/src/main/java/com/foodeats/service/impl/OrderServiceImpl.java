package com.foodeats.service.impl;

import com.foodeats.dto.CheckoutRequest;
import com.foodeats.model.*;
import com.foodeats.repository.*;
import com.foodeats.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                           CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           MerchantRepository merchantRepository,
                           UserRepository userRepository,
                           PaymentRepository paymentRepository,
                           NotificationRepository notificationRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.merchantRepository = merchantRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public Order placeOrder(Long customerId, CheckoutRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Optional<Cart> cartOpt = cartRepository.findByCustomerId(customerId);
        if (cartOpt.isEmpty() || cartOpt.get().getItems().isEmpty()) {
            throw new IllegalStateException("Shopping cart is empty");
        }

        Cart cart = cartOpt.get();
        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found"));

        double subtotal = cart.getItems().stream()
                .mapToDouble(item -> item.getMenuItem().getPrice() * item.getQuantity())
                .sum();

        double totalAmount = subtotal + merchant.getDeliveryFee();

        Order order = new Order();
        order.setCustomer(customer);
        order.setMerchant(merchant);
        order.setTotalAmount(totalAmount);
        order.setDeliveryFee(merchant.getDeliveryFee());
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress(request.getDeliveryAddress() != null 
                ? request.getDeliveryAddress() : "Phnom Penh City");
        order.setPaymentStatus("PAID");
        order.setCreatedAt(LocalDateTime.now());

        for (CartItem ci : cart.getItems()) {
            OrderItem oi = new OrderItem(order, ci.getMenuItem(), ci.getQuantity(), ci.getMenuItem().getPrice());
            order.getItems().add(oi);
        }

        Order savedOrder = orderRepository.save(order);

        Payment payment = new Payment(savedOrder, request.getPaymentMethod(), 
                "TXN-" + System.currentTimeMillis(), totalAmount, "SUCCESS");
        paymentRepository.save(payment);

        if (merchant.getOwner() != null) {
            notificationRepository.save(new Notification(
                merchant.getOwner(), 
                "New Order #" + savedOrder.getId(), 
                "You received a new order for $" + String.format("%.2f", totalAmount)
            ));
        }

        cartItemRepository.deleteByCartId(cart.getId());

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(Long userId, String userRole) {
        UserRole role = UserRole.valueOf(userRole);
        
        switch (role) {
            case MERCHANT:
                Optional<Merchant> merchantOpt = merchantRepository.findByOwnerId(userId);
                return merchantOpt.map(m -> 
                    orderRepository.findByMerchantIdOrderByCreatedAtDesc(m.getId())
                ).orElse(List.of());
            case DRIVER:
                return orderRepository.findByDriverIdOrderByCreatedAtDesc(userId);
            case ADMIN:
                return orderRepository.findAll();
            default:
                return orderRepository.findByCustomerIdOrderByCreatedAtDesc(userId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);

        notificationRepository.save(new Notification(
            order.getCustomer(), 
            "Order #" + order.getId() + " Status", 
            "Your order status is now " + newStatus.name()
        ));

        return updated;
    }

    @Override
    @Transactional
    public Order assignDriverToOrder(Long orderId, Long driverId) {
        User driver = userRepository.findById(driverId)
                .filter(u -> u.getRole() == UserRole.DRIVER)
                .orElseThrow(() -> new IllegalArgumentException("Invalid driver"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.setDriver(driver);
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        Order updated = orderRepository.save(order);

        notificationRepository.save(new Notification(
            order.getCustomer(), 
            "Driver Assigned!", 
            driver.getName() + " is delivering your order #" + order.getId()
        ));

        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAvailableDeliveryJobs() {
        return orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.ACCEPTED);
    }
}
