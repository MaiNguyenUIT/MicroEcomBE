package com.example.order_service.event;

import com.example.order_service.model.ProductQuantity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StockUpdateEvent {
    private Long orderId;
    private String orderGroupId;
    List<ProductQuantity> productQuantities = new ArrayList<>();
}

