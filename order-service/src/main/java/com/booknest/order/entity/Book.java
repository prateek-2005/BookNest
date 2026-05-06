package com.booknest.order.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class Book {

    private Long productId;
    private String productName;

    // --- Getters and Setters ---
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
