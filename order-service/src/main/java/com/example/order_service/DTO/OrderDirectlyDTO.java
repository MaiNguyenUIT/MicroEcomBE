package com.example.order_service.DTO;

import com.example.order_service.ENUM.PAYMENT_METHOD;
import com.example.order_service.model.ShippingAddress;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDirectlyDTO {
    private LocalDateTime orderDateTime = LocalDateTime.now();
    private List<Long> couponIds;
    private ShippingAddress shippingAddress;
    private PAYMENT_METHOD paymentMethod;
    private String productId;
    private int productQuantity;
}
