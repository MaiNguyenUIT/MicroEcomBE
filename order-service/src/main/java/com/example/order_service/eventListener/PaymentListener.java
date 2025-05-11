package com.example.order_service.eventListener;

import com.example.order_service.ENUM.ORDER_STATUS;
import com.example.order_service.ENUM.PAYMENT_METHOD;
import com.example.order_service.event.PaymentEvent;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Component
public class PaymentListener {
    @Autowired
    private OrderRepository orderRepository;
    @Bean
    public Consumer<PaymentEvent> paymentSuccess(){
        return event -> {
            Order order = orderRepository.findById(event.getOrderId()).orElseThrow(
                    () -> new NotFoundException("Order is not found with id: " + event.getOrderId())
            );
            System.out.println("Update order status");
            order.setOrderStatus(ORDER_STATUS.PAID);
            order.setPaymentMethod(PAYMENT_METHOD.ONLINE);
            orderRepository.save(order);
        };
    }
    @Bean
    public Consumer<String> paymentFail(){
        return event -> {
            System.out.println("Payment fail");
        };
    }
}
