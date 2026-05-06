package com.booknest.wishlist.service;

import com.booknest.wishlist.entity.Wishlist;
import com.booknest.wishlist.entity.WishlistItem;
import com.booknest.wishlist.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final RestTemplate restTemplate;

    public WishlistServiceImpl(WishlistRepository wishlistRepository, RestTemplate restTemplate) {
        this.wishlistRepository = wishlistRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public Wishlist getWishlistByUser(Long userId) {
        return wishlistRepository.findByUserId(userId).orElse(null);
    }

    @Override
    public Wishlist addBook(Long userId, Long bookId, String title, Double price, String coverImageUrl, String isbn) {
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wishlist newWishlist = new Wishlist();
                    newWishlist.setUserId(userId);
                    newWishlist.setCreatedAt(LocalDateTime.now());
                    return newWishlist;
                });

        WishlistItem item = new WishlistItem();
        item.setBookId(bookId);
        item.setBookTitle(title);
        item.setBookPrice(price);
        item.setCoverImageUrl(coverImageUrl);
        item.setIsbn(isbn);
        item.setWishlist(wishlist);

        wishlist.getItems().add(item);
        return wishlistRepository.save(wishlist);
    }

    @Override
    public Wishlist removeBook(Long userId, Long itemId) {
        Wishlist wishlist = getWishlistByUser(userId);
        if (wishlist != null) {
            wishlist.getItems().removeIf(item -> item.getItemId().equals(itemId));
            return wishlistRepository.save(wishlist);
        }
        return null;
    }

    @Override
    @jakarta.transaction.Transactional
    public void clearWishlist(Long userId) {
        Wishlist wishlist = wishlistRepository.findByUserId(userId).orElse(null);
        if (wishlist != null) {
            wishlist.getItems().clear();
            wishlistRepository.save(wishlist);
        }
    }

    @Override
    public Wishlist moveToCart(Long userId, Long itemId) {
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wishlist not found for user " + userId));

        // Find item in wishlist
        WishlistItem item = wishlist.getItems().stream()
                .filter(i -> i.getItemId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in wishlist"));

        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("bookId", item.getBookId());
        cartItem.put("bookTitle", item.getBookTitle());
        cartItem.put("price", item.getBookPrice());
        cartItem.put("quantity", 1);
        cartItem.put("coverImageUrl", item.getCoverImageUrl());
        cartItem.put("isbn", item.getIsbn());

        // Call Cart-Service (via Eureka/Gateway)
        restTemplate.postForObject(
            "http://CART-SERVICE/cart/" + userId + "/add",
            cartItem,
            String.class
        );

        // Remove item from wishlist
        wishlist.getItems().remove(item);
        return wishlistRepository.save(wishlist);
    }

    @Override
    public List<Wishlist> getAllWishlists() {
        return wishlistRepository.findAll();
    }

	@Override
	public Object getWishlistByUserId(long l) {
		// TODO Auto-generated method stub
		return null;
	}
}
