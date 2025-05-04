package com.example.order_service.service;

import com.example.order_service.DTO.CartDTO;
import com.example.order_service.DTO.CartItemDTO;
import com.example.order_service.DTO.OrderDTO;
import com.example.order_service.ENUM.ORDER_STATUS;
import com.example.order_service.client.CartClient;
import com.example.order_service.event.StockUpdateEvent;
import com.example.order_service.exception.BadRequestException;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderItem;
import com.example.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public class OrderServiceImpl implements OrderService{
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartClient cartClient;

    private final StreamBridge streamBridge;

    public OrderServiceImpl(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public Order createOrder(OrderDTO orderDTO) {
        CartDTO cart = cartClient.getUserCart();
        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Cart is empty with user id: " + SecurityContextHolder.getContext().getAuthentication().getName());
        }

        for (CartItemDTO item : cart.getCartItems()) {
            sendStockUpdate(item.getProductId(), item.getQuantity());
        }

        Order order = OrderMapper.INSTANCE.toEntity(orderDTO);
        order.setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
        for (CartItemDTO i : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(i.getQuantity());
            orderItem.setProductId(i.getProductId());
            order.getOrderItems().add(orderItem);
        }

        order.setOrderAmount(cart.getTotalPrice());

        return orderRepository.save(order);
    }

    public void sendStockUpdate(String productId, int quantity) {
        StockUpdateEvent event = new StockUpdateEvent();
        event.setProductId(productId);
        event.setQuantity(quantity);
        streamBridge.send("stockUpdate-out-0", event);
    }

    @Override
    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

    }

    @Override
    public List<Order> getOrderByUserId() {
        return orderRepository.findByUserId(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Override
    public Order cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));
        if (!order.getUserId().equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
            throw new BadRequestException("You are not authorized to cancel this order: " + orderId);
        }
        if(order.getOrderStatus() != ORDER_STATUS.PENDING){
            throw new BadRequestException("Cannot cancel this order: " + orderId);
        } else {
            order.setOrderStatus(ORDER_STATUS.CANCELLED);
            return orderRepository.save(order);
        }
    }

    @Override
    public List<Order> getAllOrder() {
        return orderRepository.findAll();
    }

    @Override
    public Order updateOrderStatus(String orderId, ORDER_STATUS orderStatus) {
        return null;
    }
}
