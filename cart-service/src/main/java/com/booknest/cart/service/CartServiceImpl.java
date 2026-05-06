package com.booknest.cart.service;

import com.booknest.cart.entity.Cart;
import com.booknest.cart.entity.CartItem;
import com.booknest.cart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Override
    public Cart getCartByUser(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));
    }

    @Override
    public Cart addItem(Long userId, CartItem item) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart c = new Cart();
            c.setUserId(userId);
            c.setItems(new ArrayList<>());
            c.setTotalPrice(0.0);
            return c;
        });

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        CartItem existingItem = cart.getItems().stream()
                .filter(i -> i.getBookId().equals(item.getBookId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int currentQty = existingItem.getQuantity() == null ? 0 : existingItem.getQuantity();
            int addQty = item.getQuantity() == null ? 1 : item.getQuantity();
            existingItem.setQuantity(currentQty + addQty);
            if (item.getPrice() != null) {
                existingItem.setPrice(item.getPrice());
            }
            if (item.getBookTitle() != null) {
                existingItem.setBookTitle(item.getBookTitle());
            }
            if (item.getCoverImageUrl() != null) {
                existingItem.setCoverImageUrl(item.getCoverImageUrl());
            }
        } else {
            CartItem newItem = new CartItem();
            newItem.setBookId(item.getBookId());
            newItem.setBookTitle(item.getBookTitle());
            newItem.setPrice(item.getPrice());
            newItem.setQuantity(item.getQuantity() == null ? 1 : item.getQuantity());
            newItem.setCoverImageUrl(item.getCoverImageUrl());
            newItem.setCart(cart);
            cart.getItems().add(newItem);
        }

        updateTotal(cart);
        return cartRepository.save(cart);
    }


    @Override
    public Cart removeItem(Long userId, Long itemId) {
        Cart cart = getCartByUser(userId);
        cart.getItems().removeIf(i -> i.getItemId().equals(itemId));
        updateTotal(cart);
        return cartRepository.save(cart);
    }

    @Override
    public Cart updateQuantity(Long userId, Long itemId, int quantity) {
        Cart cart = getCartByUser(userId);
        cart.getItems().forEach(i -> {
            if (i.getItemId().equals(itemId)) {
                i.setQuantity(quantity);
            }
        });
        updateTotal(cart);
        return cartRepository.save(cart);
    }

    @Override
    public void clearCart(Long userId) {
        Cart cart = getCartByUser(userId);
        cart.getItems().clear();
        cart.setTotalPrice(0.0);
        cartRepository.save(cart);
    }

    @Override
    public Double cartTotal(Long userId) {
        return getCartByUser(userId).getTotalPrice();
    }

    @Override
    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }

    private void updateTotal(Cart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
        cart.setTotalPrice(total);
    }
}
