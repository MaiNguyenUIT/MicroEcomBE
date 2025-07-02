package com.example.order_service.event;

import com.example.order_service.model.ProductQuantity;
import lombok.Data;

@Data
public class StockUpdateDirectlyEvent {
    private Long orderId;
    private String orderGroupId;
    ProductQuantity productQuantity;
}
