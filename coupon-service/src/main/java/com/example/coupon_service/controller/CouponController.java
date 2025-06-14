package com.example.coupon_service.controller;

import com.example.coupon_service.dto.CouponCreateRequestDTO;
import com.example.coupon_service.dto.CouponResponseDTO;
import com.example.coupon_service.dto.CouponUpdateRequestDTO;
import com.example.coupon_service.ENUM.UserRole;
import com.example.coupon_service.service.CouponService;
import com.example.coupon_service.utils.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;


import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController 
@RequestMapping("/api/coupons")
@Validated
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

 
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<CouponResponseDTO> createCoupon(
            @Valid @RequestBody CouponCreateRequestDTO request) {

        
        CouponResponseDTO createdCoupons = couponService.createCoupon(request);

        return new ResponseEntity<>(createdCoupons, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<CouponResponseDTO> updateCoupon(
            @PathVariable String id,
            @RequestBody CouponUpdateRequestDTO request) {


        CouponResponseDTO updatedCoupons = couponService.updateCoupon(id,request);

        return new ResponseEntity<>(updatedCoupons, HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<Void> softDeleteCoupon(
            @PathVariable String id) {

        
        couponService.softDeleteCoupon(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<List<CouponResponseDTO>> getAllCoupons() {

        
        List<CouponResponseDTO> coupons = couponService.getAllCouponsByRole();

        return new ResponseEntity<>(coupons, HttpStatus.OK);
    }
}