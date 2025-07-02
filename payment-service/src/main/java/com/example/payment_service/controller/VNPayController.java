package com.example.payment_service.controller;

import com.example.payment_service.DTO.OrderDTO;
import com.example.payment_service.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/vnpay")
public class VNPayController {

    @Value("${vnpay.tmn_code}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash_secret}")
    private String vnp_HashSecret;

    @Value("${vnpay.pay_url}")
    private String vnp_Url;

    @Value("${vnpay.return_url}")
    private String vnp_ReturnUrl;

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create-payment")
    public ResponseEntity<String> createPayment(@RequestBody OrderDTO orderDTO) {
        return ResponseEntity.ok(paymentService.createVNPayUrl(orderDTO));
    }

    @GetMapping("/payment-return")
    public ResponseEntity<String> paymentReturn(@RequestParam Map<String, String> queryParams) {
        return ResponseEntity.ok(paymentService.returnVNPay(queryParams));
    }
}
