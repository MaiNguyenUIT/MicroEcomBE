package com.example.user_service.service;

import com.example.user_service.dto.*;
import com.example.user_service.model.User;

import java.util.List;

public interface UserService {
    AuthResponse login(LoginRequest loginRequest);
    void register(RegisterRequest registerRequest);
    public User findUserByJwtToken();
    public User findUserByUserId(String userId) throws Exception;
    public List<User> getAllUser();
    List<User> getAllSeller();
    public User updateUserInformation(UserInforDTO userInfor, User user);
    public User blockUser(String userId);
}
