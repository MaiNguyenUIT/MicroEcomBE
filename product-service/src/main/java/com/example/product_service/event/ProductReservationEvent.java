package com.example.product_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReservationEvent {
    private String productId;
    private long orderId;
    private int quantity;
}
