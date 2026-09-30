package com.foodeats.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "merchant_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "owner"})
    private Merchant merchant;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User driver;

    @Column(name = "gross_amount", nullable = false)
    private Double grossAmount;

    @Column(name = "subtotal", nullable = false)
    private Double subtotal;

    @Column(name = "delivery_fee", nullable = false)
    private Double deliveryFee = 1.50;

    @Column(name = "commission_rate", nullable = false)
    private Double commissionRate = 0.15; // 15% platform take-rate

    @Column(name = "platform_fee", nullable = false)
    private Double platformFee;

    @Column(name = "merchant_payout", nullable = false)
    private Double merchantPayout;

    @Column(name = "driver_payout", nullable = false)
    private Double driverPayout;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING"; // PENDING, SETTLED, PAID_OUT

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Settlement() {}

    public Settlement(Order order, Merchant merchant, User driver, Double subtotal, Double deliveryFee, Double commissionRate) {
        this.order = order;
        this.merchant = merchant;
        this.driver = driver;
        this.subtotal = subtotal != null ? subtotal : 0.0;
        this.deliveryFee = deliveryFee != null ? deliveryFee : 0.0;
        this.grossAmount = this.subtotal + this.deliveryFee;
        this.commissionRate = commissionRate != null ? commissionRate : 0.15;

        // 15% platform commission on food subtotal
        this.platformFee = Math.round(this.subtotal * this.commissionRate * 100.0) / 100.0;
        // Merchant receives 85% of food subtotal
        this.merchantPayout = Math.round((this.subtotal - this.platformFee) * 100.0) / 100.0;
        // Driver receives 100% of delivery fee
        this.driverPayout = this.deliveryFee;

        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Merchant getMerchant() { return merchant; }
    public void setMerchant(Merchant merchant) { this.merchant = merchant; }

    public User getDriver() { return driver; }
    public void setDriver(User driver) { this.driver = driver; }

    public Double getGrossAmount() { return grossAmount; }
    public void setGrossAmount(Double grossAmount) { this.grossAmount = grossAmount; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; }

    public Double getCommissionRate() { return commissionRate; }
    public void setCommissionRate(Double commissionRate) { this.commissionRate = commissionRate; }

    public Double getPlatformFee() { return platformFee; }
    public void setPlatformFee(Double platformFee) { this.platformFee = platformFee; }

    public Double getMerchantPayout() { return merchantPayout; }
    public void setMerchantPayout(Double merchantPayout) { this.merchantPayout = merchantPayout; }

    public Double getDriverPayout() { return driverPayout; }
    public void setDriverPayout(Double driverPayout) { this.driverPayout = driverPayout; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
