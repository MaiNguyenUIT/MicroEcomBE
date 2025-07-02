package com.example.product_service.event;

import com.example.product_service.model.ProductQuantity;
import lombok.Data;

@Data
public class StockUpdateDirectlyEvent {
    private Long orderId;
    private String orderGroupId;
    ProductQuantity productQuantity;
}
