package com.example.order_service.model;

import java.math.BigDecimal;

import com.example.order_service.ENUM.CouponType;

import lombok.Data;

@Data
public class CouponItem {
    private String couponId;
    private String sellerId;
    private String code;
    private BigDecimal discount;
    private CouponType type;
}
