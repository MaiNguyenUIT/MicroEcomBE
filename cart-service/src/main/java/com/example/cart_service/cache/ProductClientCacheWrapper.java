package com.example.cart_service.cache;

import com.example.cart_service.DTO.ProductDTO;
import com.example.cart_service.client.ProductClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductClientCacheWrapper {
    @Autowired
    private ProductClient productClient;

    @Cacheable(value = "productCache", key = "#id")
    public ProductDTO getProductById(String id) {
        return productClient.getProductById(id);
    }

    @Cacheable(value = "productBulkCache", key = "#ids.hashCode()")
    public List<ProductDTO> getProductsByIds(List<String> ids) {
        return productClient.getProductsByIds(ids);
    }
}
