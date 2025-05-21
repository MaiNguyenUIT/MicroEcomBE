package com.example.rating_service.mapper;

import com.example.rating_service.DTO.RatingDTO;
import com.example.rating_service.model.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RatingMapper {
    RatingMapper INSTANCE = Mappers.getMapper(RatingMapper.class);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "productId", ignore = true)
    Rating toEntity(RatingDTO ratingDTO);

    RatingDTO toDTO(Rating rating);
}
