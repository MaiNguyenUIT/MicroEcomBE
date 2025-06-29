package com.example.payment_service.event;

import com.example.payment_service.ENUM.PAYMENT_STATUS;
import com.example.payment_service.ENUM.PAYMENT_TYPE;
import lombok.Data;

@Data
public class PaymentEvent {
    private Long orderId;
    private PAYMENT_STATUS paymentStatus;
    private PAYMENT_TYPE paymentType;
}
