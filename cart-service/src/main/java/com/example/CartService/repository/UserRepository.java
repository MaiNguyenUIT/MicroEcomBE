package com.example.CartService.repository;

import com.example.CartService.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    public User findByemail(String email);
}
