package com.example.order_service.eventListener;

import com.example.order_service.ENUM.ORDER_STATUS;
import com.example.order_service.ENUM.PAYMENT_METHOD;
import com.example.order_service.ENUM.PAYMENT_TYPE;
import com.example.order_service.event.PaymentEvent;
import com.example.order_service.event.ReleaseProductReservationEvent;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class PaymentListener {
    @Autowired
    private OrderRepository orderRepository;

    private final StreamBridge streamBridge;
    public PaymentListener(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Bean
    public Consumer<PaymentEvent> paymentSuccess(){
        return event -> {
            Order order = orderRepository.findByIdWithItems(event.getOrderId()).orElseThrow(
                    () -> new NotFoundException("Order is not found with id: " + event.getOrderId())
            );
            System.out.println("Update order status");
            if(event.getPaymentType().equals(PAYMENT_TYPE.PAYMENT_THEN_ORDER)){
                ReleaseProductReservationEvent releaseProductReservationEvent = new ReleaseProductReservationEvent();
                releaseProductReservationEvent.setOrderId(order.getId());
                releaseProductReservationEvent.setProductId(order.getOrderItems().get(0).getProductId());
                //create queue to release + update product
                streamBridge.send("releaseProductReservation-out-0", releaseProductReservationEvent);
            } else {
                order.setOrderStatus(ORDER_STATUS.PAID);
                order.setPaymentMethod(PAYMENT_METHOD.ONLINE);
                orderRepository.save(order);
            }
        };
    }
    @Bean
    public Consumer<String> paymentFail(){
        return event -> {
            System.out.println("Payment fail");
        };
    }
}
