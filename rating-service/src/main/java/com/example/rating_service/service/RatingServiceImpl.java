package com.example.rating_service.service;

import com.example.rating_service.DTO.OrderDTO;
import com.example.rating_service.DTO.ProductDTO;
import com.example.rating_service.DTO.RatingDTO;
import com.example.rating_service.client.OrderClient;
import com.example.rating_service.client.ProductClient;
import com.example.rating_service.event.UpdateProductRating;
import com.example.rating_service.exception.NotFoundException;
import com.example.rating_service.mapper.RatingMapper;
import com.example.rating_service.model.Rating;
import com.example.rating_service.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RatingServiceImpl implements RatingService{
    @Autowired
    private RatingRepository ratingRepository;

    private final StreamBridge streamBridge;

    public RatingServiceImpl(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Autowired
    private ProductClient productClient;

    @Autowired
    private OrderClient orderClient;

    @Override
    public List<Rating> createRating(RatingDTO ratingDTO) {
        List<Rating> ratings = new ArrayList<>();
        for (String productId : ratingDTO.getProductIds()){
            Rating rating = RatingMapper.INSTANCE.toEntity(ratingDTO);
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();

            ProductDTO productDTO = productClient.getProductById(productId);
            if (productDTO == null){
                throw new NotFoundException("Product is not found with id: " + productId);
            }

            OrderDTO orderDTO = orderClient.getOrderById(ratingDTO.getOrderId());
            if (orderDTO == null) {
                throw new NotFoundException("Order is not found with id: " + ratingDTO.getOrderId());
            }
            rating.setProductId(productId);
            rating.setOwnerId(userId);
            ratings.add(ratingRepository.save(rating));

            UpdateProductRating updateProductRating = new UpdateProductRating();
            updateProductRating.setRatingStar(rating.getRatingStar());
            updateProductRating.setProductId(productId);
            streamBridge.send("updateAverageRatingAdd-out-0", updateProductRating);
        }

        return ratings;
    }

    @Override
    public void deleteRating(String ratingId) {
        Rating rating = ratingRepository.findById(ratingId).orElseThrow(
                () -> new NotFoundException("Rating is not found with id: " + ratingId)
        );

        UpdateProductRating updateProductRating = new UpdateProductRating();
        updateProductRating.setRatingStar(rating.getRatingStar());
        updateProductRating.setProductId(rating.getProductId());
        streamBridge.send("updateAverageRatingDelete-out-0", updateProductRating);

        ratingRepository.deleteById(ratingId);
    }

    @Override
    public List<Rating> getAllRatingByProductId(String productId) {
        return ratingRepository.findRatingsByProductIdSorted(productId, Sort.by(Sort.Direction.DESC, "reviewDate"));
    }
}
