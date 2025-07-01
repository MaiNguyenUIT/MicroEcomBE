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

    CouponResponseDTO createCoupon(CouponCreateRequestDTO request);
    CouponResponseDTO updateCoupon(Long id, CouponUpdateRequestDTO updateDTO);
    void softDeleteCoupon(Long id);
    CouponResponseDTO getCouponById(Long id);
    CouponResponseDTO getCouponByCode(String code);
    CouponValidationResponse getCouponsValidationRequest(CouponsRequest couponsRequest);
    List<CouponResponseDTO> getAllCouponsByRole();

}