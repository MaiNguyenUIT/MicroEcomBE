package com.example.order_service.event;

import com.example.order_service.ENUM.PAYMENT_STATUS;
import com.example.order_service.ENUM.PAYMENT_TYPE;
import lombok.Data;

@Data
public class PaymentEvent {
    private Long orderId;
    private PAYMENT_STATUS paymentStatus;
    private PAYMENT_TYPE paymentType;
}
