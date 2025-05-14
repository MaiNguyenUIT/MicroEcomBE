package com.example.cart_service.event;

import lombok.Data;

@Data
public class ClearCartEvent {
    private String userId;
    private String groupId;
}
