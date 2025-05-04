package com.example.order_service.model;

import com.example.order_service.ENUM.ORDER_STATUS;
import com.example.order_service.ENUM.PAYMENT_METHOD;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Dùng Long auto-increment
    private Long id;

    private String userId;

    private LocalDateTime orderDateTime;

    private int orderAmount;

    @Enumerated(EnumType.STRING)
    private ORDER_STATUS orderStatus = ORDER_STATUS.PENDING;

    private String coupon;

    @Embedded
    private ShippingAddress shippingAddress;

    @Enumerated(EnumType.STRING)
    private PAYMENT_METHOD paymentMethod;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
}
