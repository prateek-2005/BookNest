// WishlistService.java
package com.booknest.wishlist.service;

import com.booknest.wishlist.entity.Wishlist;

public interface WishlistService {
    Wishlist getWishlistByUser(Long userId);
    Wishlist addBook(Long userId, Long bookId, String title, Double price, String coverImageUrl, String isbn);
    Wishlist removeBook(Long userId, Long itemId);
    void clearWishlist(Long userId);
    Wishlist moveToCart(Long userId, Long itemId);
    java.util.List<Wishlist> getAllWishlists();
	Object getWishlistByUserId(long l);
}
