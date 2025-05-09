package com.example.product_service.controller;

import com.example.product_service.dto.ProductResponse;
import com.example.product_service.model.Product;
import com.example.product_service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id){
        ProductResponse product = productService.getProduct(id);
        return ResponseEntity.ok(product);
    }
    @GetMapping()
    public ResponseEntity<List<ProductResponse>> getAllProduct(){
        List<ProductResponse> products = productService.getAllActiveProduct();
        return ResponseEntity.ok(products);
    }
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductByCategory(@PathVariable String categoryId){
        List<ProductResponse> products = productService.filterProductByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<ProductResponse>> getProductInCart(@RequestBody List<String> ids){
        List<ProductResponse> products = productService.getProductInCart(ids);
        return ResponseEntity.ok(products);
    }
}
