package com.example.product_service.dto;

import com.example.product_service.ENUM.PRODUCT_STATE;
import lombok.Data;

import java.util.List;

@Data
public class ProductResponse {
    private String id;
    private String name;
    private String categoryName;
    private int quantity;
    private int regularPrice;
    private int price;
    private String description;
    private List<String> image;
    private PRODUCT_STATE productState;
}
