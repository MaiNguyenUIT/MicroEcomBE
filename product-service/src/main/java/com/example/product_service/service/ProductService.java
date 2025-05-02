package com.example.product_service.service;

import com.example.product_service.dto.ProductDTO;
import com.example.product_service.dto.ProductResponse;
import com.example.product_service.model.Product;

import java.util.List;

public interface ProductService {
    ProductDTO createProduct(ProductDTO productDTO); //seller create
    ProductDTO updateProduct(ProductDTO productDTO, String id); //seller update
    void deleteProduct(String id);
    ProductResponse getProduct(String id);
    List<ProductResponse> getAllProduct();
    List<ProductResponse> filterProductByCategory(String categoryId);
    List<ProductResponse> getAllActiveProduct();
    List<ProductResponse> findProductsByOwnerId();
    Product approveProduct(String productId);
    List<Product> getProductInCart(List<String> productIds);
}
