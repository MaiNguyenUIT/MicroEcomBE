package com.example.order_service.mapper;

import com.example.order_service.DTO.OrderDTO;
import com.example.order_service.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderMapper {
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);
    @Mapping(target = "orderStatus", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "orderAmount", ignore = true)
    Order toEntity(OrderDTO dto);

    OrderDTO toDTO(Order order);
}
