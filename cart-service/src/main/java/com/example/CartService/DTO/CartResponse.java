package com.example.CartService.DTO;

import java.util.List;

import com.example.CartService.model.CartItem;

import lombok.Data;

@Data
public class CartResponse {
    private List<CartItem> items;
    private double subTotal;
    private double discount;
    private double total;
    private String coupon;
    private String paymentMethod;
}