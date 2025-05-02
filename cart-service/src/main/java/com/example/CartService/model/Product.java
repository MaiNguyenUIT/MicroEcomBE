package com.example.CartService.model;

import com.example.CartService.ENUM.PRODUCT_STATE;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(
        "products"
)
public class Product {
    @Id
    private String id;
    private String name;
    private String categoryId;
    private int quantity;
    private int regularPrice;
    private int price;
    private String description;
    private List<String> image;
    private PRODUCT_STATE productState;
}
