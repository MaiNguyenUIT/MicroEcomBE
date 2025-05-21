package com.example.rating_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
public class Rating {
    @Id
    private String id;
    private String review;
    private int ratingStar;
    private String ownerId;
    private String orderId;
    private String productId;
    private LocalDateTime ratingDate;
}
