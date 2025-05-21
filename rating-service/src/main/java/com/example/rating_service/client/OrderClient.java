package com.example.rating_service.client;

import com.example.rating_service.DTO.OrderDTO;
import com.example.rating_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service",
        url = "http://localhost:4444/api/order",
        configuration = FeignConfig.class
)
public interface OrderClient {
    @GetMapping("/{id}")
    OrderDTO getOrderById(@PathVariable("id") String id);
}
