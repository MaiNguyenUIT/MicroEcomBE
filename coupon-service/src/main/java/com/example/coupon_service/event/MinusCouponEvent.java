package com.example.coupon_service.event;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MinusCouponEvent {
    List<Long> couponIds = new ArrayList<>();
}
