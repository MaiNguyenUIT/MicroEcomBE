package com.example.order_service.event;

import lombok.Data;

@Data
public class AfterStockUpdateEvent {
    private Long orderId;
    private String orderGroupId;
}
