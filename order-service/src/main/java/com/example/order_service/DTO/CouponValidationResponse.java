package com.example.order_service.dto;

import java.util.List;
import lombok.Data;
import com.example.order_service.model.CouponItem;

@Data
@Builder
public class CouponValidationResponse {
    private boolean success;
    private List<CouponItem> validCoupons;
    private List<String> invalidCouponIds;
}