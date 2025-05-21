package com.example.rating_service.DTO;

import lombok.Data;

import java.util.List;

@Data
public class ProductDTO {
    private String id;
    private String name;
    private String categoryId;
    private int quantity;
    private int sold = 0;
    private int regularPrice;
    private int price;
    private String description;
    private List<String> image;
    private String ownerId;
    private int totalRating = 0;
    private double averageRating = 0;
    private boolean isApprove = false;
}
