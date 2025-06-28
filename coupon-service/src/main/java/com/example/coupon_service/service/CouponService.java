package com.example.coupon_service.service;

import com.example.coupon_service.model.Coupon;
import com.example.coupon_service.dto.CouponCreateRequestDTO;
import com.example.coupon_service.dto.CouponResponseDTO;
import com.example.coupon_service.dto.CouponUpdateRequestDTO;
import com.example.coupon_service.dto.CouponsRequest;
import com.example.coupon_service.dto.CouponValidationResponse;
import com.example.coupon_service.repository.CouponRepository;
import com.example.coupon_service.ENUM.UserRole;

import java.util.List;
import java.util.Optional;

public interface CouponService {

    public CouponResponseDTO createCoupon(CouponCreateRequestDTO request);
    public CouponResponseDTO updateCoupon(Long id, CouponUpdateRequestDTO updateDTO);
    public void softDeleteCoupon(Long id);
    
    public CouponResponseDTO getCouponById(Long id);
    public CouponResponseDTO getCouponByCode(String code);
    public CouponValidationResponse getCouponsValidationRequest(CouponsRequest couponsRequest);
    // public void applyCoupon(CouponsRequest couponsRequest);
    public List<CouponResponseDTO> getAllCouponsByRole();

}