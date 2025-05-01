package com.example.user_service.service;

import com.example.user_service.dto.AuthResponse;
import com.example.user_service.dto.LoginRequest;
import com.example.user_service.dto.RegisterRequest;
import com.example.user_service.model.User;

public interface UserService {
    AuthResponse login(LoginRequest loginRequest);
    void register(RegisterRequest registerRequest);
    User getUserById(String userId);
    User getUserByUserName(String email);
}
