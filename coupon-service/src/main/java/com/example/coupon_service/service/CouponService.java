package com.example.coupon_service.service;

import com.example.coupon_service.model.Coupon;
import com.example.coupon_service.dto.CouponCreateRequestDTO;
import com.example.coupon_service.dto.CouponResponseDTO;
import com.example.coupon_service.dto.CouponUpdateRequestDTO;
import com.example.coupon_service.repository.CouponRepository;
import com.example.coupon_service.ENUM.UserRole;

import java.util.List;
import java.util.Optional;

public interface CouponService {

    public CouponResponseDTO createCoupon(CouponCreateRequestDTO request);
    public CouponResponseDTO updateCoupon(Long id, CouponUpdateRequestDTO updateDTO);
    public void softDeleteCoupon(Long id);
    // public CouponResponseDTO applyCoupon(String code, Long userId);
    // public Optional<Coupon> getCouponById(Long id);
    // public Optional<Coupon> getCouponByCode(String code);
    public List<CouponResponseDTO> getAllCouponsByRole();

}