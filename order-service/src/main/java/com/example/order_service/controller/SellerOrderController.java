package com.example.order_service.controller;

import com.example.order_service.DTO.UpdateOrderStatusRequest;
import com.example.order_service.model.Order;
import com.example.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller/order")
public class SellerOrderController {
    @Autowired
    private OrderService orderService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestBody UpdateOrderStatusRequest request) throws Exception{
        Order apiResult = orderService.updateOrderStatus(id, request.getOrderStatus());
        return new ResponseEntity<>(apiResult, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    @GetMapping()
    public ResponseEntity<List<Order>> getAllOrder() throws Exception{
        List<Order> apiResult = orderService.getOrderBySellerId();
        return new ResponseEntity<>(apiResult, HttpStatus.OK);
    }
}
