package com.foodeats.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "merchant_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "owner"})
    private Merchant merchant;

    @Column(name = "category_name", nullable = false, length = 100)
    private String name;

    @Column(name = "display_order")
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

    @JsonProperty("name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategoryName() { return name; }
    public void setCategoryName(String categoryName) { this.name = categoryName; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Long getMerchantId() {
        return merchant != null ? merchant.getId() : null;
    }

    public String getMerchantName() {
        return merchant != null ? merchant.getName() : null;
    }
}
