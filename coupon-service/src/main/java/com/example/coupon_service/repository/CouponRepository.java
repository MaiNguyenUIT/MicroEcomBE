package com.example.coupon_service.repository;

import com.example.coupon_service.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, String> {
    long countByCreatedByUserIdAndCreatedAtBetween(String userId, LocalDateTime start, LocalDateTime end);
    Optional<Coupon> findByCodeAndIsDeletedFalse(String code);
    Optional<Coupon> findByIdAndIsDeletedFalse(String id);
    List<Coupon> findBySellerIdAndIsDeletedFalse(String sellerId);
    List<Coupon> findBySellerIdNullAndIsDeletedFalse();
    Optional<Coupon> findById(String id);
}