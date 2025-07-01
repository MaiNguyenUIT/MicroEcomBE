package com.example.order_service.utils;

import com.example.order_service.DTO.CouponsRequest;
import com.example.order_service.ENUM.CouponType;
import com.example.order_service.model.CouponItem;
import java.util.List;

public class CouponMapperUtil {
    public static CouponsRequest toCouponsRequest(List<Long> couponIds) {
        return new CouponsRequest(couponIds);
    }
}