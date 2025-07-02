package com.example.order_service.DTO;

import com.example.order_service.ENUM.PAYMENT_TYPE;
import lombok.Data;

@Data
public class PaymentDTO {
    private Long orderId;
    private int orderAmount;
    private PAYMENT_TYPE paymentType;
}
