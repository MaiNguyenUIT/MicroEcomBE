package com.example.coupon_service.dto;

import com.example.coupon_service.ENUM.CouponType;
import com.example.coupon_service.ENUM.DiscountStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponseDTO {

    private String id;

    private String code;

    private BigDecimal discountPercentage;

    private Integer usageLimit;

    private Integer currentUsage;

    private LocalDateTime expiryDate;

    private DiscountStatus status;

    private BigDecimal minPurchaseAmount;

    private CouponType couponType;

    private String sellerId;
}