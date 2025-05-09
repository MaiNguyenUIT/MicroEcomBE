package com.example.product_service.mapper;

import com.example.product_service.dto.ProductDTO;
import com.example.product_service.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sold", ignore = true)
    Product toEntity(ProductDTO productDTO);

    ProductDTO toDTO(Product product);
}
