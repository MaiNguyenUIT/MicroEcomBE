package com.example.product_service.mapper;

import com.example.product_service.dto.ProductResponse;
import com.example.product_service.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductResMapper {
    ProductResMapper INSTANCE = Mappers.getMapper(ProductResMapper.class);
    @Mapping(target = "categoryName", ignore = true )
    ProductResponse toRes(Product product);
}
