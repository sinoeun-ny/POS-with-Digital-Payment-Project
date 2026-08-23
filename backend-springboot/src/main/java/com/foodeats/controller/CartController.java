package com.foodeats.controller;

import com.foodeats.dto.CartItemRequest;
import com.foodeats.model.*;
import com.foodeats.repository.*;
import com.foodeats.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public CartController(CartRepository cartRepository, CartItemRepository cartItemRepository,
                          MenuItemRepository menuItemRepository, UserRepository userRepository,
                          JwtUtil jwtUtil) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    private User getAuthenticatedUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) return null;
        return userRepository.findById(userId).orElse(null);
    }

    @GetMapping
    public ResponseEntity<?> getCart(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getAuthenticatedUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        Cart cart = cartRepository.findByCustomerId(user.getId())
                .orElseGet(() -> cartRepository.save(new Cart(user)));

        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<?> addItemToCart(@RequestHeader("Authorization") String authHeader, @RequestBody CartItemRequest request) {
        User user = getAuthenticatedUser(authHeader);
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        Cart cart = cartRepository.findByCustomerId(user.getId())
                .orElseGet(() -> cartRepository.save(new Cart(user)));

        Optional<MenuItem> menuItemOpt = menuItemRepository.findById(request.getMenuItemId());
        if (menuItemOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "Menu item not found"));

        MenuItem menuItem = menuItemOpt.get();

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getMenuItem().getId().equals(menuItem.getId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existing = existingItemOpt.get();
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            cartItemRepository.save(existing);
        } else {
            CartItem newItem = new CartItem(cart, menuItem, request.getQuantity());
            cart.getItems().add(newItem);
            cartRepository.save(cart);
        }

        return ResponseEntity.ok(cartRepository.findByCustomerId(user.getId()).orElse(cart));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeItem(@RequestHeader("Authorization") String authHeader, @PathVariable Long itemId) {
        User user = getAuthenticatedUser(authHeader);
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        cartItemRepository.deleteById(itemId);
        return ResponseEntity.ok(Map.of("message", "Item removed"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@RequestHeader("Authorization") String authHeader) {
        User user = getAuthenticatedUser(authHeader);
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        Optional<Cart> cartOpt = cartRepository.findByCustomerId(user.getId());
        if (cartOpt.isPresent()) {
            cartItemRepository.deleteByCartId(cartOpt.get().getId());
        }
        return ResponseEntity.ok(Map.of("message", "Cart cleared"));
    }
}
