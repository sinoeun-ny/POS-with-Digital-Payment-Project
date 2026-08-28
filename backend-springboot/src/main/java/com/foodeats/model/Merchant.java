package com.foodeats.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "merchants")
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User owner;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;
    private String bannerUrl;
    private String phone;
    private String cuisineType = "Various";
    private String openingHours = "08:00 AM - 10:00 PM";
    private Double rating = 4.8;
    private Double deliveryFee = 1.50;
    private Integer deliveryTimeMins = 25;
    private Boolean isOpen = true;
    private String status = "Active"; // "Active", "Inactive"
    private String address;
    private String city = "Phnom Penh";

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
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }

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
}
