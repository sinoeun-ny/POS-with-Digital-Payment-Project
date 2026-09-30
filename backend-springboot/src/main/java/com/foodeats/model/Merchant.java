package com.foodeats.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "merchants")
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User owner;

    @Column(name = "store_name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "store_logo_url", length = 500)
    private String imageUrl;

    @Column(name = "store_banner_url", length = 500)
    private String bannerUrl;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "city", length = 100)
    private String city = "Phnom Penh";

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "cuisine_type", length = 100)
    private String cuisineType = "Various";

    @Column(name = "opening_hours", length = 100)
    private String openingHours = "08:00 AM - 10:00 PM";

    @Column(name = "rating")
    private Double rating = 4.8;

    @Column(name = "delivery_fee")
    private Double deliveryFee = 1.50;

    @Column(name = "delivery_time_mins")
    private Integer deliveryTimeMins = 25;

    @Column(name = "is_open")
    private Boolean isOpen = true;

    @Column(name = "status", length = 50)
    private String status = "Active"; // "Active", "Inactive"

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Merchant() {}

    public Merchant(User owner, String name, String description, String imageUrl, Double rating, Double deliveryFee, Integer deliveryTimeMins, Boolean isOpen, String address) {
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.rating = rating != null ? rating : 4.8;
        this.deliveryFee = deliveryFee != null ? deliveryFee : 1.50;
        this.deliveryTimeMins = deliveryTimeMins != null ? deliveryTimeMins : 25;
        this.isOpen = isOpen != null ? isOpen : true;
        this.status = "Active";
        this.address = address;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    @JsonProperty("name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStoreName() { return name; }
    public void setStoreName(String storeName) { this.name = storeName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @JsonProperty("imageUrl")
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStoreLogoUrl() { return imageUrl; }
    public void setStoreLogoUrl(String storeLogoUrl) { this.imageUrl = storeLogoUrl; }

    @JsonProperty("bannerUrl")
    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }

    public String getStoreBannerUrl() { return bannerUrl; }
    public void setStoreBannerUrl(String storeBannerUrl) { this.bannerUrl = storeBannerUrl; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCuisineType() { return cuisineType; }
    public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }

    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; }

    public Integer getDeliveryTimeMins() { return deliveryTimeMins; }
    public void setDeliveryTimeMins(Integer deliveryTimeMins) { this.deliveryTimeMins = deliveryTimeMins; }

    public Boolean getIsOpen() { return isOpen; }
    public void setIsOpen(Boolean isOpen) { this.isOpen = isOpen; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStatus() { return status != null ? status : "Active"; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
