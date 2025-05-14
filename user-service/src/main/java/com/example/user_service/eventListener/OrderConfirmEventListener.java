package com.example.user_service.eventListener;

import com.example.user_service.event.OrderConfirmEvent;
import com.example.user_service.event.SendConfirmEmailEvent;
import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class OrderConfirmEventListener {
    @Autowired
    private UserRepository userRepository;

    private final StreamBridge streamBridge;

    public OrderConfirmEventListener(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Bean
    public Consumer<OrderConfirmEvent> sendToGetFullConfirmOrder(){
        return event -> {
            User user = userRepository.findById(event.getUserId()).orElse(null);
            if(user == null){
                return;
            } else {
                SendConfirmEmailEvent sendConfirmEmailEvent = new SendConfirmEmailEvent();
                sendConfirmEmailEvent.setId(event.getId());
                sendConfirmEmailEvent.setOrderStatus(event.getOrderStatus());
                sendConfirmEmailEvent.setOrderAmount(event.getOrderAmount());
                sendConfirmEmailEvent.setUserEmail(user.getEmail());
                sendConfirmEmailEvent.setUserPhone(user.getPhone());
                sendConfirmEmailEvent.setUserDisplayName(user.getDisplayName());
                streamBridge.send("sendConfirmOrderMail-out-0", sendConfirmEmailEvent);
            }
        };
    }
}
