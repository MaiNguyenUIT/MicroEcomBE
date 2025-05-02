package com.example.product_service.repository;

import com.example.product_service.ENUM.PRODUCT_STATE;
import com.example.product_service.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    Product findByname(String productName);
    @Query(value = "{ 'name': ?0, '_id': { $ne: ?1 } }", exists = true)
    boolean existsByNameAndNotId(String name, String id);
    List<Product> findBycategoryId(String categoryId);
    List<Product> findByproductState(PRODUCT_STATE state);
    List<Product> findByusername(String username);
    List<Product> findByProductStateAndIsApprove(PRODUCT_STATE productState, boolean isApprove);
    List<Product> findByCategoryIdAndProductStateAndIsApprove(String categoryId, PRODUCT_STATE productState, boolean isApprove);
}
