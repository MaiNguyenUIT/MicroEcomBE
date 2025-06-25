package com.example.order_service.DTO;

import com.example.order_service.model.CouponItem;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CouponsRequest {
    List<CouponItem> coupons;
}