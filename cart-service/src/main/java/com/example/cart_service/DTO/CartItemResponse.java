package com.example.cart_service.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartItemResponse {
    private String productId;
    private String productName;
    private String productCategory;
    private int regularPrice;
    private int price;
    private String description;
    private List<String> image;
    private int quantity;
    private LocalDateTime addedAt;
    private String ownerId;
}
