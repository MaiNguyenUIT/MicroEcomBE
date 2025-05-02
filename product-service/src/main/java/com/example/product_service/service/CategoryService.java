package com.example.product_service.service;

import com.example.product_service.dto.CategoryDTO;
import com.example.product_service.model.Category;

import java.util.List;

public interface CategoryService {
    CategoryDTO createCategory(CategoryDTO categoryDTO) throws Exception;
    List<Category> getAllCategory();
    CategoryDTO getCategoryById(String id);
    void deleteCategoryById(String id);
    CategoryDTO updateCategory(CategoryDTO categoryDTO, String id) throws Exception;
}
