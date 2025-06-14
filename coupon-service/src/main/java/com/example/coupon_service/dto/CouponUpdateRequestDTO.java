package com.example.coupon_service.dto;

import com.example.coupon_service.model.Discount;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.coupon_service.ENUM.DiscountStatus;
import lombok.Data;

@Data
public class CouponUpdateRequestDTO {
    @NotNull(message = "Discount percentage is required.")
    @DecimalMin(value = "0.01", message = "Discount percentage must be at least 0.01 (1%).")
    @DecimalMax(value = "1.0", message = "Discount percentage cannot exceed 1.0 (100%).")
    private BigDecimal discountPercentage;
    private Integer usageLimit;
    private BigDecimal minPurchaseAmount;
    private LocalDateTime expiryDate;
    private DiscountStatus status;
    private Boolean isDeleted;
}
