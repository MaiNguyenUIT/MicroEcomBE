package com.example.user_service.event;

import com.example.user_service.dto.ORDER_STATUS;
import lombok.Data;

@Data
public class SendConfirmEmailEvent {
    private Long id;
    private ORDER_STATUS orderStatus;
    private int orderAmount;
    private String userEmail;
    private String userPhone;
    private String userDisplayName;
}
