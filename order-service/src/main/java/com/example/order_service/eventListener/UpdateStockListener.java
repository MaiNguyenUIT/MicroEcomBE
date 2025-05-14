package com.example.order_service.eventListener;

import com.example.order_service.ENUM.ORDER_STATUS;
import com.example.order_service.event.AfterStockUpdateEvent;
import com.example.order_service.event.ClearCartEvent;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.repository.OrderTrackerRepository;
import com.example.order_service.utils.SagaTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

//need update
@Component
public class UpdateStockListener {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderTrackerRepository orderTrackerRepository;
    @Autowired
    private SagaTracker sagaTracker;
    private final StreamBridge streamBridge;
    public UpdateStockListener(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }
    @Bean
    public Consumer<AfterStockUpdateEvent> stockUpdateSuccess(){
        return event -> {
            Order order = orderRepository.findById(event.getOrderId()).orElseThrow(
                    () -> new NotFoundException("Order is not found with id: " + event.getOrderId())
            );
            sagaTracker.markOrderAsSuccess(event.getOrderGroupId(), event.getOrderId());

            if(sagaTracker.isAllOrdersSuccessful(event.getOrderGroupId())){
                ClearCartEvent clearCartEvent = new ClearCartEvent();
                clearCartEvent.setGroupId(event.getOrderGroupId());
                clearCartEvent.setUserId(order.getUserId());
                streamBridge.send("clearCart-out-0", clearCartEvent);
                sagaTracker.cleanup(event.getOrderGroupId());
            }
        };
    }

    @Bean
    public Consumer<AfterStockUpdateEvent> stockUpdateFail(){
        return event -> {
            Order order = orderRepository.findById(event.getOrderId()).orElseThrow(
                    () -> new NotFoundException("Order is not found with id: " + event.getOrderId())
            );
            System.out.println("Stock update fail");
            order.setOrderStatus(ORDER_STATUS.FAIL);
            orderRepository.save(order);
        };
    }
}
