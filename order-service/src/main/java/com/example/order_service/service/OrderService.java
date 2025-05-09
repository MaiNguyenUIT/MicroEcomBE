package com.example.order_service.service;

import com.example.order_service.DTO.OrderDTO;
import com.example.order_service.ENUM.ORDER_STATUS;
import com.example.order_service.model.Order;

import java.util.List;

public interface OrderService {
    List<Order> createOrder(OrderDTO orderDTO);
    Order getOrderById(Long orderId);
    List<Order> getOrderByUserId();
    Order cancelOrder(Long orderId);
    List<Order> getAllOrder();
    Order updateOrderStatus(Long orderId, ORDER_STATUS orderStatus);
    List<Order> getOrderBySellerId();
}
