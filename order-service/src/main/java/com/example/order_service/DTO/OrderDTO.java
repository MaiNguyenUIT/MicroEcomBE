package com.example.order_service.DTO;

import com.example.order_service.ENUM.PAYMENT_METHOD;
import com.example.order_service.model.ShippingAddress;

import java.time.LocalDateTime;

public class OrderDTO {
    private LocalDateTime orderDateTime = LocalDateTime.now();
    private String coupon;
    private ShippingAddress shippingAddress;
    private PAYMENT_METHOD paymentMethod;
}
