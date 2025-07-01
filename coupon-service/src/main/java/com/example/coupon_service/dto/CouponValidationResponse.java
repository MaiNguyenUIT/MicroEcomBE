package com.example.coupon_service.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.coupon_service.model.CouponItem;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponValidationResponse {
    private boolean success;
    private List<CouponItem> validCoupons;
    private List<Long> invalidCouponIds;
}