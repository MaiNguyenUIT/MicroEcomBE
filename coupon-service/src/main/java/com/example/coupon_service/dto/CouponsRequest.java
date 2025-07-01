package com.example.coupon_service.dto;

import com.example.coupon_service.model.CouponItem;
import com.example.coupon_service.ENUM.CouponType;
import java.util.List;
import lombok.Data;


@Data
public class CouponsRequest {
    List<Long> couponIds;
}
