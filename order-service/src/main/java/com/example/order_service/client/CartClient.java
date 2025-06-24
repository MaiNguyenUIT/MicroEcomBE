package com.example.order_service.client;

import com.example.order_service.DTO.CartDTO;
import com.example.order_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "cart-service",
        path = "/api/cart",
        configuration = FeignConfig.class
)
public interface CartClient {

    @GetMapping("/user")
    CartDTO getUserCart();
}
