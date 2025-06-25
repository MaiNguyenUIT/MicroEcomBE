package com.example.coupon_service.dto;

import com.example.coupon_service.model.CouponItem;
import java.util.List;
import lombok.Data;

@Data
public class CouponsRequest {
    List<CouponItem> coupons;
}
