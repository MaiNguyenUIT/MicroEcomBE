package com.example.order_service.DTO;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Data
public class CartDTO {
    @Id
    private String id;
    private int totalPrice;
    private List<CartItemDTO> cartItems = new ArrayList<>();
    private int totalItem;
    private String userId;
}
