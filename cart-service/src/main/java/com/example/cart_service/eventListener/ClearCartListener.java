package com.example.cart_service.eventListener;

import com.example.cart_service.event.ClearCartEvent;
import com.example.cart_service.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class ClearCartListener {
    @Autowired
    private CartRepository cartRepository;

    private final StreamBridge streamBridge;
    public ClearCartListener(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }
    @Bean
    public Consumer<ClearCartEvent> clearCart(){
        return event -> {
            cartRepository.deleteByuserId(event.getUserId());
            System.out.println("📥 Nhận message từ RabbitMQ: " +event.getUserId());
            ClearCartEvent clearCartEvent = new ClearCartEvent();
            clearCartEvent.setGroupId(event.getGroupId());
            clearCartEvent.setUserId(event.getUserId());
            streamBridge.send("clearCartSuccess-out-0", clearCartEvent);
        };
    }
}
