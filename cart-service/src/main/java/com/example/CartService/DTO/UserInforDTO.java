package com.example.CartService.DTO;

import com.example.CartService.model.Address;
import lombok.Data;

@Data
public class UserInforDTO {
    private String phone;
    private String photo;
    private Address address;
}
