package com.example.CartService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.CartService.model.User;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/users/info")
    User getUserFromToken(@RequestHeader("Authorization") String jwt);
}