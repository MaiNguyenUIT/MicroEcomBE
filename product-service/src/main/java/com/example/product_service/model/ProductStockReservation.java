package com.example.product_service.model;

import com.example.product_service.ENUM.PRODUCT_RESERVATION_STATE;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "ProductStockReservation", timeToLive = 900)
@Data
public class ProductStockReservation {
    @Id
    private String id; //orderId + productId
    private String productId;
    private long orderId;
    private int quantity;
    private PRODUCT_RESERVATION_STATE state = PRODUCT_RESERVATION_STATE.HELD;
}
