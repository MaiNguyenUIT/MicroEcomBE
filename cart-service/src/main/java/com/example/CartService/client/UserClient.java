package com.example.CartService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.CartService.DTO.UserDTO;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/user/from-token")
    UserDTO getUserFromToken(@RequestHeader("Authorization") String token);
}