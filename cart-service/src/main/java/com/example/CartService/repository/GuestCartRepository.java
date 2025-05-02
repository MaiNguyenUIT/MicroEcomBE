package com.example.CartService.repository;

import com.example.CartService.model.GuestCart;
import org.springframework.data.repository.CrudRepository;


public interface GuestCartRepository extends CrudRepository<GuestCart, String> {
    void deleteBysessionId(String id);
}
