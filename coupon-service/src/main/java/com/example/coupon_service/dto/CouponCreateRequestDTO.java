package com.example.coupon_service.dto;

import com.example.coupon_service.ENUM.CouponType;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponCreateRequestDTO {

    @NotNull(message = "Discount percentage is required.")
    @DecimalMin(value = "0.01", message = "Discount percentage must be at least 0.01 (1%).")
    @DecimalMax(value = "1.0", message = "Discount percentage cannot exceed 1.0 (100%).")
    private BigDecimal discountPercentage;

    @NotNull(message = "Usage limit per coupon is required.")
    @Positive(message = "Usage limit per coupon must be a positive number.")
    private Integer usageLimit;

    @NotNull(message = "Expiry date is required for all coupons in the batch.")
    private LocalDateTime expiryDate;

    @DecimalMin(value = "0.00", message = "Minimum purchase amount cannot be negative.")
    private BigDecimal minimumPurchaseAmount;
    
}