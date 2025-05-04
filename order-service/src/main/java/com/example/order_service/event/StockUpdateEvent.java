package com.example.order_service.event;

import lombok.Data;

@Data
public class StockUpdateEvent {
    private String productId;
    private int quantity;
}

