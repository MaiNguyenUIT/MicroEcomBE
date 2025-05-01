package com.example.user_service.repository;

import com.example.user_service.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {
    User findByusername(String username);
    List<User> findByRole(String role);
}
