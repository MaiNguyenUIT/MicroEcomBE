package com.example.order_service.DTO;

import com.example.order_service.model.CouponItem;
import com.example.order_service.ENUM.CouponType;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponsRequest {
    List<String> couponIds;
    CouponType couponType;
}