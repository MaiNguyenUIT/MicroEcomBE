package com.example.order_service.client;

import com.example.order_service.DTO.ProductResponse;
import com.example.order_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "product-service",
        path = "/api/product",
        configuration = FeignConfig.class
)
public interface ProductClient {
    @GetMapping("/{id}")
    ProductResponse getProductById(String id);
}
