package com.example.CartService.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.CartService.DTO.ProductDTO;

@FeignClient(name = "product-service", url = "http://localhost:8081/api/products") // Cập nhật URL nếu khác
public interface ProductClient {

    @GetMapping("/{id}")
    ProductDTO getProductById(@PathVariable("id") String id);

    @PostMapping("/bulk")
    List<ProductDTO> getProductsByIds(@RequestBody List<String> ids);
}
