package com.example.CartService.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CartItem {
    private String productId;
    private int quantity = 1;
    private LocalDateTime addedAt;
}