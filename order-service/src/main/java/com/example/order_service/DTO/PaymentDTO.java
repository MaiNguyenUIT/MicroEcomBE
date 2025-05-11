package com.example.order_service.DTO;

import lombok.Data;

@Data
public class PaymentDTO {
    private Long orderId;
    private int orderAmount;
}
