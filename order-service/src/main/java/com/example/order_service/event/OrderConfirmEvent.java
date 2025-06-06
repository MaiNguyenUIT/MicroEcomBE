package com.example.order_service.event;

import com.example.order_service.ENUM.ORDER_STATUS;
import lombok.Data;

@Data
public class OrderConfirmEvent {
    private Long id;
    private ORDER_STATUS orderStatus;
    private int orderAmount;
    private String userId;
}
