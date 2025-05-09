package com.example.order_service.client;

import com.example.order_service.DTO.CartDTO;
import com.example.order_service.DTO.UserDTO;
import com.example.order_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "user-service",
        url = "http://localhost:4444/api/user",
        configuration = FeignConfig.class
)
public interface UserClient {

    @GetMapping("")
    UserDTO getUserFromJwtToken();
}
