// WishlistResource.java
package com.booknest.wishlist.controller;

import com.booknest.wishlist.entity.Wishlist;
import com.booknest.wishlist.service.WishlistService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
public class WishlistResource {

    private final WishlistService wishlistService;

    public WishlistResource(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping("/{userId}")
    public Wishlist getWishlist(@PathVariable Long userId) {
        return wishlistService.getWishlistByUser(userId);
    }

    @PostMapping("/{userId}/add")
    public Wishlist addBook(@PathVariable Long userId,
                            @RequestParam Long bookId,
                            @RequestParam String title,
                            @RequestParam Double price,
                            @RequestParam(required = false) String coverImageUrl,
                            @RequestParam(required = false) String isbn) {
        return wishlistService.addBook(userId, bookId, title, price, coverImageUrl, isbn);
    }

    @DeleteMapping("/{userId}/remove/{itemId}")
    public Wishlist removeBook(@PathVariable Long userId, @PathVariable Long itemId) {
        return wishlistService.removeBook(userId, itemId);
    }

    @DeleteMapping("/{userId}/clear")
    public void clearWishlist(@PathVariable Long userId) {
        wishlistService.clearWishlist(userId);
    }

    @PostMapping("/{userId}/move/{itemId}")
    public Wishlist moveToCart(@PathVariable Long userId, @PathVariable Long itemId) {
        return wishlistService.moveToCart(userId, itemId);
    }

    @GetMapping("/all")
    public List<Wishlist> getAllWishlists() {
        return wishlistService.getAllWishlists();
    }
}
