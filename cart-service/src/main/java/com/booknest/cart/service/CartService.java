package com.booknest.cart.service;

import com.booknest.cart.entity.Cart;
import com.booknest.cart.entity.CartItem;

import java.util.List;

public interface CartService {
    Cart getCartByUser(Long userId);
    Cart addItem(Long userId, CartItem item);
    Cart removeItem(Long userId, Long itemId);
    Cart updateQuantity(Long userId, Long itemId, int quantity);
    void clearCart(Long userId);
    Double cartTotal(Long userId);
    List<Cart> getAllCarts();
}
