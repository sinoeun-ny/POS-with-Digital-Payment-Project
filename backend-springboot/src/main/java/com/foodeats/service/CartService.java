package com.foodeats.service;

import com.foodeats.model.Cart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface CartService {
    
    Cart getOrCreateCart(Long customerId);
    
    Cart addItemToCart(Long customerId, Long menuItemId, Integer quantity);
    
    void removeItem(Long itemId);
    
    void clearCart(Long customerId);
}
