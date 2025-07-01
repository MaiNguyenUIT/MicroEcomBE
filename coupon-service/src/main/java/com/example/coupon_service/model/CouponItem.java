package com.example.coupon_service.model;

import java.math.BigDecimal;

import com.example.coupon_service.ENUM.CouponType;

import lombok.Data;

@Data
public class CouponItem {
    private Long couponId;
    private String createdByUserId;
    private String code;
    private BigDecimal discount;
    private CouponType couponType;
    private BigDecimal minPurchaseAmount;
}
