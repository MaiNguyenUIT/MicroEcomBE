package com.example.order_service.eventListener;

import com.example.order_service.DTO.UserDTO;
import com.example.order_service.client.UserClient;
import com.example.order_service.event.AfterStockUpdateEvent;
import com.example.order_service.event.ClearCartEvent;
import com.example.order_service.event.OrderConfirmEvent;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import org.apache.catalina.LifecycleState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component
public class ClearCartListener {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserClient userClient;
    private final StreamBridge streamBridge;
    public ClearCartListener(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }
    @Bean
    public Consumer<ClearCartEvent> clearCartSuccess(){
        return event -> {
            List<Order> orders = orderRepository.findByOrderGroupId(event.getGroupId());
            for(Order order : orders){
                OrderConfirmEvent orderConfirmEvent = new OrderConfirmEvent();
                orderConfirmEvent.setOrderStatus(order.getOrderStatus());
                orderConfirmEvent.setOrderAmount(order.getOrderAmount());
                orderConfirmEvent.setId(order.getId());
                orderConfirmEvent.setUserId(order.getUserId());
                streamBridge.send("sendToGetFullConfirmOrder-out-0", orderConfirmEvent);
            }
        };
    }
}
