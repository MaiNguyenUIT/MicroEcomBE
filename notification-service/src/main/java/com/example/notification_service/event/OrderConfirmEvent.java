package com.example.notification_service.event;

import com.example.notification_service.ENUM.ORDER_STATUS;
import lombok.Data;

@Data
public class OrderConfirmEvent {
    private Long id;
    private ORDER_STATUS orderStatus;
    private int orderAmount;
    private String userEmail;
    private String userPhone;
    private String userDisplayName;
}
