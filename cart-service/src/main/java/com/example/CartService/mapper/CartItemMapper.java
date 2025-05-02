package com.example.CartService.mapper;

import com.example.CartService.DTO.CartItemDTO;
import com.example.CartService.model.CartItem;
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
