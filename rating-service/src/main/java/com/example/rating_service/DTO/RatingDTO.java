package com.example.rating_service.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RatingDTO {
    private int ratingStar;
    private String review;
    private LocalDateTime ratingDate = LocalDateTime.now();
    private String orderId;
    private List<String> productIds;
}
