package com.example.payment_service.service;

import com.example.payment_service.DTO.OrderDTO;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

public interface PaymentService {
    String createVNPayUrl(OrderDTO orderDTO);
    String returnVNPay(@RequestParam Map<String, String> queryParams);
}
