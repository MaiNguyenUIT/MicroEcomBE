package com.example.product_service.event;

import lombok.Data;

@Data
public class ReleaseProductReservationEvent {
    private Long orderId;
    private String productId;
}
