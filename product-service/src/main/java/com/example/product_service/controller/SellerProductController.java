package com.example.product_service.controller;

import com.example.product_service.dto.ProductDTO;
import com.example.product_service.dto.ProductResponse;
import com.example.product_service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/seller/product")
public class SellerProductController {
    @Autowired
    private ProductService productService;

    @PreAuthorize("hasRole('SELLER') || hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) throws Exception{
        ProductDTO newProduct = productService.createProduct(productDTO);
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('SELLER') || hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@RequestBody ProductDTO productDTO, @PathVariable String id) throws Exception{
        ProductDTO updateProduct = productService.updateProduct(productDTO, id);
        return new ResponseEntity<>(updateProduct, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SELLER') || hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable String id) throws Exception{
        return new ResponseEntity<>("Delete product successfully", HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SELLER') || hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<List<ProductResponse>> getAllProduct(){
        List<ProductResponse> products = productService.findProductsByOwnerId();
        return ResponseEntity.ok(products);
    }
}
