package com.example.cart_service.repository;
import com.example.cart_service.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {
    Optional<Cart> findByuserId(String userId);
    void deleteByuserId(String userId);
}