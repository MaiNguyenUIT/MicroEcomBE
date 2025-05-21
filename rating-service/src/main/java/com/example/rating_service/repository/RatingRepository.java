package com.example.rating_service.repository;

import com.example.rating_service.model.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface RatingRepository extends MongoRepository<Rating, String> {
    @Query("{ 'productId': ?0 }")
    List<Rating> findRatingsByProductIdSorted(String productId, Sort sort);

    Page<Rating> findByProductIdAndRatingStar(String productId, int ratingStar, Pageable pageable);
}
