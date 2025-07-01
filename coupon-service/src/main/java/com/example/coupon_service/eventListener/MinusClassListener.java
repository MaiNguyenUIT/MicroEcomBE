package com.example.coupon_service.eventListener;

import com.example.coupon_service.ENUM.DiscountStatus;
import com.example.coupon_service.event.MinusCouponEvent;
import com.example.coupon_service.model.Coupon;
import com.example.coupon_service.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component
public class MinusClassListener {
    @Autowired
    private CouponRepository couponRepository;

    @Bean
    public Consumer<MinusCouponEvent> minusCoupon(){
        return event -> {
            List<Coupon> coupons = couponRepository.findAllById(event.getCouponIds());
            List<Coupon> updatedCoupons = new ArrayList<>();
            for(Coupon coupon : coupons){
                coupon.setUsageLimit(coupon.getUsageLimit() - 1);
                coupon.setCurrentUsage(coupon.getCurrentUsage() + 1);
                if(coupon.getUsageLimit() == 0){
                    coupon.setStatus(DiscountStatus.USED_UP);
                }
                updatedCoupons.add(coupon);
            }
            couponRepository.saveAll(updatedCoupons);
        };
    }
}
