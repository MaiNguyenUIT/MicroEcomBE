package com.example.product_service.event;

import lombok.Data;

@Data
public class RatingUpdateEvent {
    private String productId;
    private int ratingStar;
}
