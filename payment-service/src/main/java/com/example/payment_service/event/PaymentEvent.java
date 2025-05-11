package com.example.payment_service.event;

import com.example.payment_service.ENUM.PAYMENT_STATUS;
import lombok.Data;

@Data
public class PaymentEvent {
    private Long orderId;
    private PAYMENT_STATUS paymentStatus;
}
