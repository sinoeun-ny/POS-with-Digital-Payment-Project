package com.foodeats.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "merchant_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "owner"})
    private Merchant merchant;

    @Column(nullable = false)
    private String name;

    private Integer displayOrder = 0;

    public Category() {}

    public Category(Merchant merchant, String name) {
        this.merchant = merchant;
        this.name = name;
        this.displayOrder = 0;
    }

    public Category(Merchant merchant, String name, Integer displayOrder) {
        this.merchant = merchant;
        this.name = name;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Merchant getMerchant() { return merchant; }
    public void setMerchant(Merchant merchant) { this.merchant = merchant; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Long getMerchantId() {
        return merchant != null ? merchant.getId() : null;
    }

    public String getMerchantName() {
        return merchant != null ? merchant.getName() : null;
    }
}
