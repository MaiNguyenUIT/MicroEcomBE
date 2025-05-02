package com.example.product_service.model;

import com.example.product_service.ENUM.PRODUCT_STATE;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
public class Product {
    @Id
    private String id;
    private String name;
    private String categoryId;
    private int quantity;
    private int sold = 0;
    private int regularPrice;
    private int price;
    private String description;
    private List<String> image;
    private PRODUCT_STATE productState;
    private String ownerId;
    private boolean isApprove = false;
}
