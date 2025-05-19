package com.example.cart_service.DTO;

import lombok.Data;

@Data
public class UpdateQuantityDTO {
    private String productId;
    private int quantity;
}
