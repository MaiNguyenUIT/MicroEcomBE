package com.example.order_service.model;

import java.math.BigDecimal;

import com.example.order_service.ENUM.CouponType;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Embeddable
@Getter
@Setter
public class CouponItem {
    private Long couponId;
    private String createdByUserId;
    private String code;
    private BigDecimal discount;
    private CouponType couponType;
    private BigDecimal minPurchaseAmount;
}
