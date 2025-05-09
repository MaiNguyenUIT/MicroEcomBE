package com.example.order_service.event;

import com.example.order_service.ENUM.ORDER_STATUS;
import lombok.Data;

@Data
public class OrderUpdateStatusEvent {
    private Long id;
    private ORDER_STATUS orderStatus;
    private String userEmail;
    private String userPhone;
    private String userDisplayName;
}
