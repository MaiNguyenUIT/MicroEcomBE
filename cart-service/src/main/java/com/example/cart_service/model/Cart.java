package com.example.cart_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;


@Data
public class Cart {
    @Id
    private String id;
    private int totalPrice;
    private List<CartItem> cartItems = new ArrayList<>();
    private int totalItem;
    private String userId;
}