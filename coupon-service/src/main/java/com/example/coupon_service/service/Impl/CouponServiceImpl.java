package com.example.coupon_service.service.Impl;

import com.example.coupon_service.mapper.CouponMapper;
import com.example.coupon_service.model.CodeGenerationConfig;
import com.example.coupon_service.model.Discount;
import com.example.coupon_service.model.Coupon;
import com.example.coupon_service.repository.CouponRepository;
import com.example.coupon_service.service.CouponService;

import com.example.coupon_service.dto.CouponCreateRequestDTO;
import com.example.coupon_service.dto.CouponResponseDTO;
import com.example.coupon_service.dto.CouponUpdateRequestDTO;
import com.example.coupon_service.utils.CouponCodeGenerator;
import com.example.coupon_service.utils.SecurityUtils;
import com.example.coupon_service.ENUM.CouponErrorReason;
import com.example.coupon_service.ENUM.DiscountStatus;
import com.example.coupon_service.ENUM.CouponType;
import com.example.coupon_service.ENUM.UserRole;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.coupon_service.exception.NotFoundException;
import com.example.coupon_service.exception.BadRequestException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    private static final int MAX_CODE_GENERATION_RETRIES = 5;

    private final CodeGenerationConfig adminCodeConfig;
    private final CodeGenerationConfig sellerCodeConfig;
    private final CodeGenerationConfig defaultCodeConfig;

    public CouponServiceImpl (CouponRepository couponRepository,
                          CouponMapper couponMapper) {
        this.couponMapper = couponMapper;
        this.couponRepository = couponRepository;

        this.adminCodeConfig = CodeGenerationConfig.builder()
                                .prefix("ADM-")
                                .length(9)
                                .characterSet("0123456789")
                                .build();

        this.sellerCodeConfig = CodeGenerationConfig.builder()
                                .prefix("SEL-")
                                .length(8)
                                .build();
        this.defaultCodeConfig = CodeGenerationConfig.builder()
                                .length(12)
                                .build();
    }

    private CodeGenerationConfig getCodeConfigForRole(UserRole role) {
        if (role == UserRole.ADMIN) {
            return adminCodeConfig;
        } else if (role == UserRole.SELLER) {
            return sellerCodeConfig;
        }
        return defaultCodeConfig;
    }

    @Override
    @Transactional
    public CouponResponseDTO createCoupon(
            CouponCreateRequestDTO request           
    ) {
        String creatorUserId = SecurityUtils.getCurrentUserId();
        UserRole creatorRole = SecurityUtils.getCurrentUserRole();

        if (creatorUserId == null || creatorRole == null) {
            throw new IllegalArgumentException("Creator User ID and role must be provided.");
        }

        CouponCreationDetails couponDetails = getCouponCreationDetails(creatorRole);

        return generateAndSaveCoupons(
                request,
                couponDetails.effectiveCouponType,
                creatorUserId,
                getCodeConfigForRole(creatorRole)
        );
    }

    @Override
    @Transactional
    public CouponResponseDTO updateCoupon(Long id, CouponUpdateRequestDTO updateDTO) {
        String updaterUserId = SecurityUtils.getCurrentUserId();
        UserRole updaterRole = SecurityUtils.getCurrentUserRole();

        if (id == null || id <= 0) {
            throw new BadRequestException("Coupon ID must be provided.");
        }        
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new NotFoundException("Coupon not found with ID: " + id));
        
        if (UserRole.SELLER.equals(updaterRole)) {
            if (!coupon.getCreatedByUserId().equals(updaterUserId)) {
                throw new IllegalArgumentException("Seller is not authorized to update coupons not created by them.");
            }
        }

        couponMapper.toEntity(updateDTO, coupon);
        couponRepository.save(coupon);
        return couponMapper.toResponseDTO(coupon);
    }
    
    @Override
    @Transactional
    public void softDeleteCoupon(Long id) {
        String deleterUserId = SecurityUtils.getCurrentUserId();
        UserRole deleterRole = SecurityUtils.getCurrentUserRole();

        Coupon coupon = couponRepository.findByIdAndIsDeletedFalse(id)
                                .orElseThrow(() -> new NotFoundException("Coupon not found with ID: " + id));
       
        if (UserRole.SELLER.equals(deleterRole)) {
            if (coupon.getCreatedByUserId() == null || !coupon.getCreatedByUserId().equals(deleterUserId)) {
                throw new IllegalArgumentException("Seller is not authorized to delete coupons not created by them.");
            }
        }


        if (coupon.getStatus() == DiscountStatus.ACTIVE || coupon.getStatus() == DiscountStatus.USED_UP) {
            throw new BadRequestException("Cannot delete coupon in current status (" + coupon.getStatus() + "). Please change its status first.");
        }

        coupon.setIsDeleted(true);
        couponRepository.save(coupon);
    }

    public List<CouponResponseDTO> getAllCouponsByRole() {

        String getorUserId = SecurityUtils.getCurrentUserId();
        UserRole getorRole = SecurityUtils.getCurrentUserRole();

        if (getorRole == null) {
            throw new BadRequestException("User role must be provided.");
        }

        List<Coupon> coupons;

        if (UserRole.ADMIN.equals(getorRole)) {
            coupons = couponRepository.findBycouponTypeAndIsDeletedFalse(CouponType.GLOBAL);
        }
        else {
            if (getorUserId == null) {
                throw new IllegalArgumentException("Seller User ID must be provided for seller role.");
            }
            coupons = couponRepository.findBycreatedByUserIdAndIsDeletedFalse(getorUserId);
        }
        
        return coupons.stream()
                      .map(couponMapper::toResponseDTO)
                      .collect(Collectors.toList());
    }



    private static class CouponCreationDetails {
        CouponType effectiveCouponType;

        CouponCreationDetails(CouponType effectiveCouponType) {
            this.effectiveCouponType = effectiveCouponType;
        }
    }

    private CouponCreationDetails getCouponCreationDetails(UserRole creatorRole) {
        if (UserRole.ADMIN.equals(creatorRole)) {
            return new CouponCreationDetails(CouponType.GLOBAL);
        } else {
            return new CouponCreationDetails(CouponType.SELLER_SPECIFIC);
        }
    }

    private CouponResponseDTO generateAndSaveCoupons(
            CouponCreateRequestDTO request,
            CouponType effectiveCouponType,
            String creatorUserId,
            CodeGenerationConfig codeConfig
    ) {
        CouponResponseDTO createdCoupons = null;


        Coupon newCoupon = couponMapper.toEntity(request);

        newCoupon.setCouponType(effectiveCouponType);
        newCoupon.setCreatedByUserId(creatorUserId);
        newCoupon.setCreatedAt(LocalDateTime.now());
        newCoupon.setIsDeleted(false);
        
        boolean codeGeneratedAndSavedSuccessfully = false;
        for (int retry = 0; retry < MAX_CODE_GENERATION_RETRIES; retry++) {
            String generatedCode = CouponCodeGenerator.generateCode(codeConfig);
            newCoupon.setCode(generatedCode);

            try {
                Coupon savedCoupon = couponRepository.save(newCoupon);
                createdCoupons = couponMapper.toResponseDTO(savedCoupon);
                codeGeneratedAndSavedSuccessfully = true;
                break;
            } catch (DataIntegrityViolationException e) {
                System.err.println("WARN: Failed to generate unique coupon code, retrying... (Attempt: " + (retry + 1) + ")");
                if (retry == MAX_CODE_GENERATION_RETRIES - 1) {
                    throw new RuntimeException("Failed to generate a unique coupon code after " + MAX_CODE_GENERATION_RETRIES + " retries for coupon batch.", e);
                }
            }
        }

        if (!codeGeneratedAndSavedSuccessfully) {
            System.err.println("ERROR: A coupon could not be generated and saved after all retries. Batch might be incomplete.");
        }
        
        return createdCoupons;
    }
}