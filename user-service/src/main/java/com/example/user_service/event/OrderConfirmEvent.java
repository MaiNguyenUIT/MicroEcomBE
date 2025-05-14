package com.example.user_service.event;

import com.example.user_service.dto.ORDER_STATUS;
import lombok.Data;

@Data
public class OrderConfirmEvent {
    private Long id;
    private ORDER_STATUS orderStatus;
    private int orderAmount;
    private String userId;
}
