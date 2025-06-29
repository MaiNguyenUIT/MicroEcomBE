package com.example.product_service.repository;

import com.example.product_service.model.ProductStockReservation;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProductStockReservationRepository extends CrudRepository<ProductStockReservation, String> {
    List<ProductStockReservation> findByProductId(String productId);
}
