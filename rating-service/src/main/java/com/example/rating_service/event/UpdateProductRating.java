package com.example.rating_service.event;

import lombok.Data;

@Data
public class UpdateProductRating {
    private String productId;
    private int ratingStar;
}
