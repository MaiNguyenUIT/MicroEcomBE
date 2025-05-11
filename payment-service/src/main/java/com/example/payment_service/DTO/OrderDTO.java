package com.example.payment_service.DTO;

import lombok.Data;

@Data
public class OrderDTO {
    private Long orderId;
    private int orderAmount;
}
