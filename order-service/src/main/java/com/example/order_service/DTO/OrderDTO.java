package com.example.order_service.DTO;

import com.example.order_service.ENUM.CouponType;
import com.example.order_service.ENUM.PAYMENT_METHOD;
import com.example.order_service.model.ShippingAddress;
import com.example.order_service.model.CouponItem;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private LocalDateTime orderDateTime = LocalDateTime.now();
    private List<String> couponIds;
    private CouponType couponType;
    private ShippingAddress shippingAddress;
    private PAYMENT_METHOD paymentMethod;
}
