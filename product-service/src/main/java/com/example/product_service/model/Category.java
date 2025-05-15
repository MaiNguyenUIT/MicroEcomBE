package com.example.product_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
public class Category {
    @Id
    private String id;
    private String title;
    private String image;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
