package com.example.cart_service.DTO;

import com.example.cart_service.ENUM.PRODUCT_STATE;
import lombok.Data;

import java.util.List;

@Data
public class ProductDTO {
    private String id;
    private String name;
    private int regularPrice;
    private int price;
    private List<String> image;
    private PRODUCT_STATE productState;
    private String ownerId;
}
