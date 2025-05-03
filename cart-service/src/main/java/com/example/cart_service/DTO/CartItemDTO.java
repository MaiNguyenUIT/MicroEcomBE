package com.example.cart_service.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CartItemDTO {
    private String productId;
    private LocalDateTime addedAt = LocalDateTime.now();
}