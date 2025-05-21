package com.example.rating_service.DTO;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private String userId;
    private LocalDateTime orderDateTime;
    private int orderAmount;
    private String sellerId;
    private String orderGroupId;
    private String coupon;
}
