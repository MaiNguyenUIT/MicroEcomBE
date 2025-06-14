package com.example.coupon_service.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
public class Discount {
    BigDecimal value;


    public Discount(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Discount percentage value must be positive.");
        }

        if (value.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Percentage discount value cannot be greater than 1 (i.e., 100%).");
        }

        this.value = value;
    }

    public BigDecimal calculateActualDiscount(BigDecimal originalPrice) {
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Original price must be a non-negative value.");
        }
        return originalPrice.multiply(value);
    }
}