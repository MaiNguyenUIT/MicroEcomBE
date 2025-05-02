package com.example.product_service.repository;

import com.example.product_service.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {
    Category findByTitle(String title);
}
