package com.example.product_service.service;

import com.example.product_service.dto.CategoryDTO;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.mapper.CategoryMapper;
import com.example.product_service.model.Category;
import com.example.product_service.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService{
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) throws Exception {
        if(categoryRepository.findByTitle(categoryDTO.getTitle()) != null){
            throw new BadRequestException("Category is already exist");
        }
        Category category = CategoryMapper.INSTANCE.toEntity(categoryDTO);
        category.setCreatedAt(LocalDateTime.now());
        return CategoryMapper.INSTANCE.toDTO(categoryRepository.save(category));
    }

    @Override
    public List<Category> getAllCategory() {
        return categoryRepository.findAll();
    }

    @Override
    public CategoryDTO getCategoryById(String id) {
        return CategoryMapper.INSTANCE.toDTO(categoryRepository.findById(id).
                orElseThrow(() -> new NotFoundException("Category not found with ID: " + id)));
    }

    @Override
    public void deleteCategoryById(String id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, String id) throws Exception {
        Category category = categoryRepository.findById(id).orElse(null);
        if(category == null){
            throw new NotFoundException("Category not found");
        }
        if(categoryRepository.findByTitle(categoryDTO.getTitle()) != null){
            throw new BadRequestException("Category is already exist");
        }
        category.setTitle(categoryDTO.getTitle());
        category.setUpdatedAt(LocalDateTime.now());
        return CategoryMapper.INSTANCE.toDTO(categoryRepository.save(category));
    }
}
