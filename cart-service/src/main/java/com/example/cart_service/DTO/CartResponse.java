package com.example.cart_service.DTO;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Data
public class CartResponse {
    @Id
    private String id;
    private int totalPrice;
    private List<CartItemResponse> cartItems = new ArrayList<>();
    private int totalItem;
    private String userId;
}