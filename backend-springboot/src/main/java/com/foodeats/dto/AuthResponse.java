package com.foodeats.dto;

import com.foodeats.model.UserRole;

public class AuthResponse {
    private String token;
    private Long userId;
    private String name;
    private String email;
    private UserRole role;
    private Long merchantId;

    public AuthResponse(String token, Long userId, String name, String email, UserRole role) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public AuthResponse(String token, Long userId, String name, String email, UserRole role, Long merchantId) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.merchantId = merchantId;
    }

    private java.util.List<com.foodeats.model.UserAddress> addresses;

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public Long getMerchantId() { return merchantId; }
    public java.util.List<com.foodeats.model.UserAddress> getAddresses() { return addresses; }
    public void setAddresses(java.util.List<com.foodeats.model.UserAddress> addresses) { this.addresses = addresses; }
}
