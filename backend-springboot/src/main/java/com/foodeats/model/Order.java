package com.foodeats.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "merchant_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "owner"})
    private Merchant merchant;

    @Column(name = "merchant_name", length = 150)
    private String merchantName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User driver;

    @Column(name = "subtotal")
    private Double subtotal;

    @Column(name = "delivery_fee")
    private Double deliveryFee = 1.50;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @Column(name = "payment_status", length = 30)
    private String paymentStatus = "PAID";

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Order() {}

    public Order(User customer, Merchant merchant, User driver, Double totalAmount, Double deliveryFee, OrderStatus status, String deliveryAddress, String paymentStatus) {
        this.customer = customer;
        this.merchant = merchant;
        if (merchant != null) {
            this.merchantName = merchant.getName();
        }
        this.driver = driver;
        this.totalAmount = totalAmount;
        this.deliveryFee = deliveryFee != null ? deliveryFee : 1.50;
        this.subtotal = (totalAmount != null && deliveryFee != null) ? (totalAmount - deliveryFee) : totalAmount;
        this.status = status != null ? status : OrderStatus.PENDING;
        this.deliveryAddress = deliveryAddress;
        this.paymentStatus = paymentStatus != null ? paymentStatus : "PAID";
        this.orderNumber = "ORD-" + System.currentTimeMillis();
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNumber() {
        if (orderNumber == null && id != null) {
            return "ORD-" + id;
        }
        return orderNumber;
    }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }

    public User getUser() { return customer; }
    public void setUser(User user) { this.customer = user; }

    public Merchant getMerchant() { return merchant; }
    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
        if (merchant != null && this.merchantName == null) {
            this.merchantName = merchant.getName();
        }
    }

    public String getMerchantName() {
        if (merchantName != null) return merchantName;
        return merchant != null ? merchant.getName() : "";
    }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    public User getDriver() { return driver; }
    public void setDriver(User driver) { this.driver = driver; }

    public Double getSubtotal() {
        if (subtotal != null) return subtotal;
        if (totalAmount != null && deliveryFee != null) return totalAmount - deliveryFee;
        return totalAmount;
    }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
