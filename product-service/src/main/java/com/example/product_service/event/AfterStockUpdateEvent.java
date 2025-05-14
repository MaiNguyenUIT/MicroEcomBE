package com.example.product_service.event;

import lombok.Data;

@Data
public class AfterStockUpdateEvent {
    private Long orderId;
    private String orderGroupId;
}
