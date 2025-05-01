package com.example.user_service.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String displayName;
    private String username;
    private String password;
}
