package com.example.coupon_service.ENUM;
public enum CouponErrorReason {
    NOT_FOUND,              // Mã coupon không tồn tại
    EXPIRED,                // Coupon đã hết hạn
    USAGE_LIMIT_REACHED,    // Coupon đã hết số lượt sử dụng
    MIN_PURCHASE_NOT_MET,   // Không đạt số tiền mua tối thiểu
    NOT_ACTIVE,             // Coupon không ở trạng thái ACTIVE
    SELLER_MISMATCH,        // Coupon chỉ áp dụng cho người bán khác
    INVALID_TYPE_FOR_SELLER // Lỗi nội bộ: coupon SELLER_SPECIFIC nhưng không có sellerId
}
