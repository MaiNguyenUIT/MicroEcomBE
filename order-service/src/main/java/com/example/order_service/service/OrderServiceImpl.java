package com.example.order_service.service;

import com.example.order_service.DTO.CartDTO;
import com.example.order_service.DTO.CartItemDTO;
import com.example.order_service.DTO.OrderDTO;
import com.example.order_service.DTO.UserDTO;
import com.example.order_service.ENUM.ORDER_STATUS;
import com.example.order_service.client.CartClient;
import com.example.order_service.client.UserClient;
import com.example.order_service.event.OrderConfirmEvent;
import com.example.order_service.event.OrderUpdateStatusEvent;
import com.example.order_service.event.StockUpdateEvent;
import com.example.order_service.exception.BadRequestException;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderItem;
import com.example.order_service.repository.OrderRepository;
import org.apache.catalina.User;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService{
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartClient cartClient;
    @Autowired
    private UserClient userClient;

    private final StreamBridge streamBridge;

    public OrderServiceImpl(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    //Sai
    @Override
    public List<Order> createOrder(OrderDTO orderDTO) {
        CartDTO cart = cartClient.getUserCart();
        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Cart is empty with user id: " + SecurityContextHolder.getContext().getAuthentication().getName());
        }

        Map<String, List<CartItemDTO>> itemsBySeller = cart.getCartItems()
                .stream()
                .collect(Collectors.groupingBy(CartItemDTO::getOwnerId));

        UserDTO userDTO = userClient.getUserFromJwtToken();
        List<Order> orders = new ArrayList<>();

        for (Map.Entry<String, List<CartItemDTO>> entry : itemsBySeller.entrySet()) {
            String sellerId = entry.getKey();
            List<CartItemDTO> sellerItems = entry.getValue();

            Order order = new Order();
            order.setCoupon(orderDTO.getCoupon());
            order.setShippingAddress(orderDTO.getShippingAddress());
            order.setPaymentMethod(orderDTO.getPaymentMethod());
            order.setOrderDateTime(orderDTO.getOrderDateTime());
            order.setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
            order.setSellerId(sellerId);
            order.setOrderItems(new ArrayList<>());


            int total = 0;

            for (CartItemDTO item : sellerItems) {
                sendStockUpdate(item.getProductId(), item.getQuantity());

                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(item.getProductId());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setOrder(order);
                order.getOrderItems().add(orderItem);

                total += item.getPrice();
            }

            order.setOrderAmount(total);
            Order createOrder = orderRepository.save(order);
            OrderConfirmEvent orderConfirmEvent = buildConfirmEvent(userDTO, createOrder.getId(), createOrder.getOrderStatus(), createOrder.getOrderAmount());
            sendConfirmEmail(orderConfirmEvent);
            orders.add(createOrder);
        }

        sendClearCart();

        return orders;
    }

    public OrderConfirmEvent buildConfirmEvent(UserDTO userDTO, Long orderId, ORDER_STATUS orderStatus, int orderAmount){
        OrderConfirmEvent orderConfirmEvent = new OrderConfirmEvent();
        orderConfirmEvent.setOrderStatus(orderStatus);
        orderConfirmEvent.setOrderAmount(orderAmount);
        orderConfirmEvent.setUserEmail(userDTO.getEmail());
        orderConfirmEvent.setUserPhone(userDTO.getPhone());
        orderConfirmEvent.setUserDisplayName(userDTO.getDisplayName());
        orderConfirmEvent.setId(orderId);
        return orderConfirmEvent;
    }

    public OrderUpdateStatusEvent buildUpdateEvent(UserDTO userDTO, Long orderId, ORDER_STATUS orderStatus){
        OrderUpdateStatusEvent orderUpdateStatusEvent = new OrderUpdateStatusEvent();
        orderUpdateStatusEvent.setId(orderId);
        orderUpdateStatusEvent.setOrderStatus(orderStatus);
        orderUpdateStatusEvent.setUserEmail(userDTO.getEmail());
        orderUpdateStatusEvent.setUserPhone(userDTO.getPhone());
        orderUpdateStatusEvent.setUserDisplayName(userDTO.getDisplayName());

        return orderUpdateStatusEvent;
    }

    public void sendStockUpdate(String productId, int quantity) {
        StockUpdateEvent event = new StockUpdateEvent();
        event.setProductId(productId);
        event.setQuantity(quantity);
        streamBridge.send("stockUpdate-out-0", event);
    }

    public void sendClearCart() {
        streamBridge.send("clearCart-out-0", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    public void sendConfirmEmail(OrderConfirmEvent orderConfirmEvent) {
        streamBridge.send("sendConfirmOrderMail-out-0", orderConfirmEvent);
    }

    public void sendUpdateEmail(OrderUpdateStatusEvent orderUpdateStatusEvent){
        streamBridge.send("sendUpdateOrderMail-out-0", orderUpdateStatusEvent);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

    }

    @Override
    public List<Order> getOrderByUserId() {
        return orderRepository.findByUserId(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Override
    public Order cancelOrder(Long orderId) {
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
    public Order updateOrderStatus(Long orderId, ORDER_STATUS orderStatus) {
        UserDTO userDTO = userClient.getUserFromJwtToken();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));
        order.setOrderStatus(orderStatus);

        OrderUpdateStatusEvent orderUpdateStatusEvent = buildUpdateEvent(userDTO, orderId, orderStatus);
        sendUpdateEmail(orderUpdateStatusEvent);
        return orderRepository.save(order);
    }

    @Override
    public List<Order> getOrderBySellerId() {
        return orderRepository.findBySellerId(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
