package com.example.product_service.controller;

import com.example.product_service.dto.ProductResponse;
import com.example.product_service.model.Product;
import com.example.product_service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin/product")
public class AdminProductController {
    @Autowired
    private ProductService productService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<List<ProductResponse>> getAllProduct(){
        List<ProductResponse> products = productService.getAllProduct();
        return ResponseEntity.ok(products);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Product> approveProduct(@PathVariable String id){
        Product product = productService.approveProduct(id);
        return ResponseEntity.ok(product);
    }
}
