package com.example.order_service.DTO;

import com.example.order_service.ENUM.CouponType;
import com.example.order_service.ENUM.DiscountStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponResponseDTO {

    private Long id;

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
