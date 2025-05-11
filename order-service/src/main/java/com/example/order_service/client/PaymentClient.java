package com.example.order_service.client;

import com.example.order_service.DTO.CartDTO;
import com.example.order_service.DTO.PaymentDTO;
import com.example.order_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "payment-service",
        url = "http://localhost:4444/api/vnpay",
        configuration = FeignConfig.class
)
public interface PaymentClient {
    @PostMapping("/create-payment")
    String createPayment(PaymentDTO paymentDTO);
}
