package com.example.notification_service.event;

import com.example.notification_service.ENUM.ORDER_STATUS;
import lombok.Data;

@Data
public class OrderStatusUpdateEvent {
    private Long id;
    private ORDER_STATUS orderStatus;
    private String userEmail;
    private String userPhone;
    private String userDisplayName;
}
