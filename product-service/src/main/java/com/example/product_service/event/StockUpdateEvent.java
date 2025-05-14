package com.example.product_service.event;

import com.example.product_service.model.ProductQuantity;
import lombok.Data;

import java.util.List;

@Data
public class StockUpdateEvent {
    private Long orderId;
    private String orderGroupId;
    List<ProductQuantity> productQuantities;
}
