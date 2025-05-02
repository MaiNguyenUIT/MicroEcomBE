package com.example.CartService.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("Cart")
public class CartRedis {
    @Id
    private String userId;
    private List<CartItem> items;
    private double subtotal;
    private double priceCoupon;
    private String paymentMethod;
    private String couponCode;
}
