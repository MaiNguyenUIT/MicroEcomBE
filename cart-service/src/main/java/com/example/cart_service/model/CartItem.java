package com.example.cart_service.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CartItem {
    private String productId;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    private int quantity = 1;
    private LocalDateTime addedAt;
    private String sellerId;
}