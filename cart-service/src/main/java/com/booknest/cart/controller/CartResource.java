package com.booknest.cart.controller;

import com.booknest.cart.entity.Cart;
import com.booknest.cart.entity.CartItem;
import com.booknest.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartResource {

    @Autowired
    private CartService cartService;

    @GetMapping("/{userId}")
    public Cart getCart(@PathVariable Long userId) {
        return cartService.getCartByUser(userId);
    }

    @PostMapping("/{userId}/add")
    public Cart addItem(@PathVariable Long userId, @RequestBody CartItem item) {
        return cartService.addItem(userId, item);
    }

    @DeleteMapping("/{userId}/remove/{itemId}")
    public Cart removeItem(@PathVariable Long userId, @PathVariable Long itemId) {
        return cartService.removeItem(userId, itemId);
    }

    @PutMapping("/{userId}/update/{itemId}")
    public Cart updateQuantity(@PathVariable Long userId, @PathVariable Long itemId, @RequestParam int quantity) {
        return cartService.updateQuantity(userId, itemId, quantity);
    }

    @DeleteMapping("/{userId}/clear")
    public void clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
    }

    @GetMapping("/{userId}/total")
    public Double cartTotal(@PathVariable Long userId) {
        return cartService.cartTotal(userId);
    }
}
