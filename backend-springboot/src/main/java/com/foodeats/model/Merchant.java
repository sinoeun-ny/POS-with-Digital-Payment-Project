package com.foodeats.model;

import jakarta.persistence.*;

@Entity
@Table(name = "merchants")
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;
    private Double rating = 4.5;
    private Double deliveryFee = 1.50;
    private Integer deliveryTimeMins = 25;
    private Boolean isOpen = true;
    private String address;

    public Merchant() {}

    public Merchant(User owner, String name, String description, String imageUrl, Double rating, Double deliveryFee, Integer deliveryTimeMins, Boolean isOpen, String address) {
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.deliveryFee = deliveryFee;
        this.deliveryTimeMins = deliveryTimeMins;
        this.isOpen = isOpen;
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
}
