package com.example.order_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private String id;
    private String name;
    private int regularPrice;
    private int price;
    private String description;
    private String ownerId;
    private int totalRating;
    private double averageRating;
}
