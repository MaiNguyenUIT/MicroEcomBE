package com.example.order_service.controller;

import com.example.order_service.DTO.OrderDTO;
import com.example.order_service.model.Order;
import com.example.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @PostMapping()
    public ResponseEntity<List<Order>> createOrder(@RequestBody OrderDTO orderDTO) throws Exception {
        List<Order> apiResult = orderService.createOrder(orderDTO);
        return new ResponseEntity<>(apiResult, HttpStatus.CREATED);
    }

    @GetMapping()
    public ResponseEntity<List<Order>> getUserOrder() throws Exception {
        List<Order> apiResult = orderService.getOrderByUserId();
        return new ResponseEntity<>(apiResult, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) throws Exception {
        Order apiResult = orderService.getOrderById(id);
        return new ResponseEntity<>(apiResult, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) throws Exception {
       Order apiResult = orderService.cancelOrder(id);
        return new ResponseEntity<>(apiResult, HttpStatus.OK);
    }
}
