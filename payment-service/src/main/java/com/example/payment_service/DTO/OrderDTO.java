package com.example.payment_service.DTO;

import com.example.payment_service.ENUM.PAYMENT_TYPE;
import lombok.Data;

@Data
public class OrderDTO {
    private Long orderId;
    private int orderAmount;
    private PAYMENT_TYPE paymentType;
}
