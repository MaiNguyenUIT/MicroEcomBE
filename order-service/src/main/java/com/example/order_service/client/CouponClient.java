package com.example.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.order_service.DTO.CouponResponseDTO;
import com.example.order_service.DTO.CouponsRequest;
import com.example.order_service.DTO.CouponValidationResponse;
import com.example.order_service.config.FeignConfig;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "coupon-service",
             url = "http://localhost:4444/api/coupons",
             configuration = FeignConfig.class
)
public interface CouponClient {
    @PostMapping("/check")
    CouponValidationResponse getCouponsValidationResponses(@RequestBody CouponsRequest request);
}
