package com.example.coupon_service.model;

import com.example.coupon_service.model.Discount;

import com.example.coupon_service.ENUM.CouponType;
import com.example.coupon_service.ENUM.DiscountStatus;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 4))
    })
    private Discount discount;

    @Column(nullable = false)
    private Integer usageLimit;

    @Column(nullable = false)
    private Integer currentUsage;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountStatus status;

    @Column(precision = 10, scale = 2)
    private BigDecimal minPurchaseAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType couponType;

    @Column(nullable = true)
    private String createdByUserId;

    @Column(nullable = true)
    private LocalDateTime createdAt;

    @ColumnDefault("false")
    @Column(nullable = false)
    private Boolean isDeleted;

}