package com.example.order_service.DTO;

import com.example.order_service.ENUM.ORDER_STATUS;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    ORDER_STATUS orderStatus;
}
