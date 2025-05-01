package com.example.user_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class User {
    @Id
    private String id;
    private String displayName;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String photo = "https://www.shutterstock.com/image-vector/default-avatar-profile-icon-social-600nw-1677509740.jpg";
    private Role role = Role.ROLE_USER;
    private Address address;
    private boolean isBlock = false;
}
