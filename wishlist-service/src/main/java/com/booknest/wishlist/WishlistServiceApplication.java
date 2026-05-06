package com.booknest.wishlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.booknest")
public class WishlistServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WishlistServiceApplication.class, args);
    }
}
