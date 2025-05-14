package com.example.order_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "OrderTracker", timeToLive = 86400) // Giữ 24h
@Data
public class OrderTracker {
    @Id
    private String id;
    private Long orderId;
    private String orderGroupId;
    private String status;
}
