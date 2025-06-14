package com.example.coupon_service.mapper;

import com.example.coupon_service.dto.CouponResponseDTO;
import com.example.coupon_service.dto.CouponCreateRequestDTO;
import com.example.coupon_service.dto.CouponUpdateRequestDTO;

import com.example.coupon_service.model.Coupon;
import com.example.coupon_service.model.Discount;

import com.example.coupon_service.ENUM.DiscountStatus;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CouponMapper {

    @Mapping(target = "discountPercentage", source = "discount.value")
    @Mapping(target = "sellerId", source = "createdByUserId")
    CouponResponseDTO toResponseDTO(Coupon coupon);

    @Mapping(target = "discount", expression = "java(new Discount(dto.getDiscountPercentage()))")
    @Mapping(target = "minPurchaseAmount", source = "minimumPurchaseAmount")
    @Mapping(target = "currentUsage", constant = "0")
    @Mapping(target = "status", expression = "java(com.example.coupon_service.ENUM.DiscountStatus.ACTIVE)")
    Coupon toEntity(CouponCreateRequestDTO dto);

    @Mapping(target = "discount", expression = "java(new Discount(dto.getDiscountPercentage()))")
    void toEntity(CouponUpdateRequestDTO dto, @MappingTarget Coupon entity);

}