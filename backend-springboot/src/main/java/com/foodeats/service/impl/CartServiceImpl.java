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

import java.time.LocalDateTime;
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
    @Transactional
    public Cart getOrCreateCart(Long customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    User customer = userRepository.findById(customerId)
                            .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + customerId));
                    Cart newCart = new Cart(customer);
                    return cartRepository.save(newCart);
                });
    }

    @Override
    @Transactional
    public Cart addItemToCart(Long customerId, Long menuItemId, Integer quantity) {
        Cart cart = getOrCreateCart(customerId);
        
        Optional<MenuItem> menuItemOpt = menuItemRepository.findById(menuItemId);
        if (menuItemOpt.isEmpty()) {
            throw new IllegalArgumentException("Menu item not found with id: " + menuItemId);
        }
        
        MenuItem menuItem = menuItemOpt.get();
        int addQty = (quantity != null && quantity > 0) ? quantity : 1;

        if (menuItem.getMerchant() != null) {
            cart.setMerchant(menuItem.getMerchant());
        } else if (menuItem.getCategory() != null && menuItem.getCategory().getMerchant() != null) {
            cart.setMerchant(menuItem.getCategory().getMerchant());
        }
        cart.setUpdatedAt(LocalDateTime.now());
        
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getMenuItem() != null && item.getMenuItem().getId().equals(menuItem.getId()))
                .findFirst();
        
        if (existingItemOpt.isPresent()) {
            CartItem existing = existingItemOpt.get();
            existing.setQuantity(existing.getQuantity() + addQty);
            cartItemRepository.save(existing);
        } else {
            CartItem newItem = new CartItem(cart, menuItem, addQty);
            newItem.setItemName(menuItem.getName());
            newItem.setPrice(menuItem.getPrice());
            cart.getItems().add(newItem);
        }
        
        Cart saved = cartRepository.save(cart);
        return cartRepository.findById(saved.getId()).orElse(saved);
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
        cartOpt.ifPresent(cart -> {
            cartItemRepository.deleteByCartId(cart.getId());
            cart.getItems().clear();
            cart.setMerchant(null);
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
        });
    }
}
