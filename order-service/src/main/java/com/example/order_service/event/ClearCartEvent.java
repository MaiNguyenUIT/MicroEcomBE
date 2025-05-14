package com.example.order_service.event;

import lombok.Data;

@Data
public class ClearCartEvent {
    private String userId;
    private String groupId;
}
