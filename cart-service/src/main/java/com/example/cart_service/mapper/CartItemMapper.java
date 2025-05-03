package com.example.cart_service.mapper;


import com.example.cart_service.DTO.CartItemDTO;
import com.example.cart_service.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


@Mapper
public interface CartItemMapper {
    CartItemMapper INSTANCE = Mappers.getMapper(CartItemMapper.class);
    @Mapping(target = "quantity", ignore = true)
    CartItem toEntity(CartItemDTO dto);

    CartItemDTO toDTO(CartItem cartItem);
}
