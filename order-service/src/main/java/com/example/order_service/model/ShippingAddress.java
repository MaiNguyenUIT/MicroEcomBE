package com.example.order_service.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class ShippingAddress {
    private String name;
    private String phone;
    private String province;
    private String district;
    private String street;
}
