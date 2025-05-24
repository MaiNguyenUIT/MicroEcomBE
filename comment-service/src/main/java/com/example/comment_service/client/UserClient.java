package com.example.comment_service.client;

import com.example.comment_service.config.FeignConfig;
import com.example.comment_service.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-service",
        url = "http://localhost:4444/api/user",
        configuration = FeignConfig.class
)
public interface UserClient {

    @GetMapping("/{id}")
    UserDTO getUserById(@PathVariable("id") String id);

    @PostMapping("/bulk")
    List<UserDTO> getUserByIds(@RequestBody List<String> ids);
}
