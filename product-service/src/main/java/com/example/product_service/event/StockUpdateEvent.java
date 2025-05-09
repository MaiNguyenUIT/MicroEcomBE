package com.example.product_service.event;

import lombok.Data;

@Data
public class StockUpdateEvent {
    private String productId;
    private int quantity;
}
