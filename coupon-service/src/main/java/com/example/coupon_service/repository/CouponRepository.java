package com.example.coupon_service.repository;

import com.example.coupon_service.model.Coupon;
import com.example.coupon_service.ENUM.CouponType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    long countByCreatedByUserIdAndCreatedAtBetween(String userId, LocalDateTime start, LocalDateTime end);
    Optional<Coupon> findByCodeAndIsDeletedFalse(String code);
    Optional<Coupon> findByIdAndIsDeletedFalse(Long id);
    List<Coupon> findBycreatedByUserIdAndIsDeletedFalse(String sellerId);
    List<Coupon> findBycouponTypeAndIsDeletedFalse(CouponType couponType);
    Optional<Coupon> findById(Long id);
}