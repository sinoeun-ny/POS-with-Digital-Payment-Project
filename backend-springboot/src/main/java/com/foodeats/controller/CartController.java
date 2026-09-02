package com.foodeats.controller;

import com.foodeats.dto.CartItemRequest;
import com.foodeats.model.Cart;
import com.foodeats.model.User;
import com.foodeats.security.JwtUtil;
import com.foodeats.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final JwtUtil jwtUtil;

    public CartController(CartService cartService, JwtUtil jwtUtil) {
        this.cartService = cartService;
        this.jwtUtil = jwtUtil;
    }

    private User getAuthenticatedUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) return null;
        User user = new User();
        user.setId(userId);
        return user;
    }

    @GetMapping
    public ResponseEntity<?> getCart(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getAuthenticatedUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        Cart cart = cartService.getOrCreateCart(user.getId());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<?> addItemToCart(@RequestHeader("Authorization") String authHeader, 
                                           @RequestBody CartItemRequest request) {
        User user = getAuthenticatedUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        try {
            Cart cart = cartService.addItemToCart(user.getId(), request.getMenuItemId(), request.getQuantity());
            return ResponseEntity.ok(cart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeItem(@RequestHeader("Authorization") String authHeader, 
                                        @PathVariable Long itemId) {
        User user = getAuthenticatedUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        cartService.removeItem(itemId);
        return ResponseEntity.ok(Map.of("message", "Item removed"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@RequestHeader("Authorization") String authHeader) {
        User user = getAuthenticatedUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        cartService.clearCart(user.getId());
        return ResponseEntity.ok(Map.of("message", "Cart cleared"));
    }
}
