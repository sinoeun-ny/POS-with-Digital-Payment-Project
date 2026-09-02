package com.foodeats.service.impl;

import com.foodeats.model.Cart;
import com.foodeats.model.CartItem;
import com.foodeats.model.MenuItem;
import com.foodeats.model.User;
import com.foodeats.repository.CartItemRepository;
import com.foodeats.repository.CartRepository;
import com.foodeats.repository.MenuItemRepository;
import com.foodeats.repository.UserRepository;
import com.foodeats.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    public CartServiceImpl(CartRepository cartRepository, 
                          CartItemRepository cartItemRepository,
                          MenuItemRepository menuItemRepository,
                          UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getOrCreateCart(Long customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    User customer = userRepository.findById(customerId)
                            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
                    return cartRepository.save(new Cart(customer));
                });
    }

    @Override
    @Transactional
    public Cart addItemToCart(Long customerId, Long menuItemId, Integer quantity) {
        Cart cart = getOrCreateCart(customerId);
        
        Optional<MenuItem> menuItemOpt = menuItemRepository.findById(menuItemId);
        if (menuItemOpt.isEmpty()) {
            throw new IllegalArgumentException("Menu item not found");
        }
        
        MenuItem menuItem = menuItemOpt.get();
        
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getMenuItem().getId().equals(menuItem.getId()))
                .findFirst();
        
        if (existingItemOpt.isPresent()) {
            CartItem existing = existingItemOpt.get();
            existing.setQuantity(existing.getQuantity() + quantity);
            cartItemRepository.save(existing);
        } else {
            CartItem newItem = new CartItem(cart, menuItem, quantity);
            cart.getItems().add(newItem);
            cartRepository.save(cart);
        }
        
        return cartRepository.findByCustomerId(customerId).orElse(cart);
    }

    @Override
    @Transactional
    public void removeItem(Long itemId) {
        cartItemRepository.deleteById(itemId);
    }

    @Override
    @Transactional
    public void clearCart(Long customerId) {
        Optional<Cart> cartOpt = cartRepository.findByCustomerId(customerId);
        cartOpt.ifPresent(cart -> cartItemRepository.deleteByCartId(cart.getId()));
    }
}
