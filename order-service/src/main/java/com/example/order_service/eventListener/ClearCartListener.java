package com.example.order_service.eventListener;

import com.example.order_service.event.ClearCartEvent;
import com.example.order_service.event.MinusCouponEvent;
import com.example.order_service.event.OrderConfirmEvent;
import com.example.order_service.model.CouponItem;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component
public class ClearCartListener {
    @Autowired
    private OrderRepository orderRepository;
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

                MinusCouponEvent minusCouponEvent = new MinusCouponEvent();
                minusCouponEvent.setCouponIds(order.getCouponIds());
                streamBridge.send("sendToGetFullConfirmOrder-out-0", orderConfirmEvent);
                streamBridge.send("minusCoupon-out-0", minusCouponEvent);
            }
        };
    }
}
