package com.example.rating_service.service;

import com.example.rating_service.DTO.RatingDTO;
import com.example.rating_service.model.Rating;

import java.util.List;

public interface RatingService {
    List<Rating> createRating(RatingDTO ratingDTO);
    void deleteRating(String ratingId);
    List<Rating> getAllRatingByProductId(String productId);
}
