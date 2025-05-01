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
    private Role role = Role.ROLE_USER;
}
