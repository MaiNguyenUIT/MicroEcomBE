package com.example.rating_service.client;

import com.example.rating_service.DTO.ProductDTO;
import com.example.rating_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service",
        path = "/api/product",
        configuration = FeignConfig.class
)
public interface ProductClient {
    @GetMapping("/{id}")
    ProductDTO getProductById(@PathVariable("id") String id);
}
