package com.example.cart_service.client;

import com.example.cart_service.DTO.ProductDTO;
import com.example.cart_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "product-service",
        url = "http://localhost:4444/api/product",
        configuration = FeignConfig.class
)
public interface ProductClient {

    @GetMapping("/{id}")
    ProductDTO getProductById(@PathVariable("id") String id);

    @PostMapping("/bulk")
    List<ProductDTO> getProductsByIds(@RequestBody List<String> ids);
}
