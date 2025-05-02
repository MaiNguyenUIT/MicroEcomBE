package com.example.CartService.repository;
import com.example.CartService.model.Cart;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CartRepository extends MongoRepository<Cart, String> {
    Optional<Cart> findByuserId(String userId);
    void deleteByuserId(String userId);
}