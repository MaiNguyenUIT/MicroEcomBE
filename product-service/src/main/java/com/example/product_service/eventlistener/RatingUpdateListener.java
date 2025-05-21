package com.example.product_service.eventlistener;

import com.example.product_service.ENUM.PRODUCT_STATE;
import com.example.product_service.event.AfterStockUpdateEvent;
import com.example.product_service.event.RatingUpdateEvent;
import com.example.product_service.event.StockUpdateEvent;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.model.Product;
import com.example.product_service.model.ProductQuantity;
import com.example.product_service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class RatingUpdateListener {
    @Autowired
    private ProductRepository productRepository;

    @Bean
    public Consumer<RatingUpdateEvent> updateAverageRatingAdd() {
        return event -> {
            System.out.println("Rating update add");
            Product product = productRepository.findById(event.getProductId()).orElseThrow(
                    () -> new NotFoundException("Product is not found with id: " + event.getProductId())
            );

            product.setAverageRating((product.getAverageRating() + event.getRatingStar()) / (product.getTotalRating() + 1));
            product.setTotalRating(product.getTotalRating() + 1);

            productRepository.save(product);
        };
    }

    @Bean
    public Consumer<RatingUpdateEvent> updateAverageRatingDelete() {
        return event -> {
            System.out.println("Rating update delete");
            Product product = productRepository.findById(event.getProductId()).orElseThrow(
                    () -> new NotFoundException("Product is not found with id: " + event.getProductId())
            );

            product.setAverageRating((product.getAverageRating() - event.getRatingStar()) / (product.getTotalRating() - 1));
            product.setTotalRating(product.getTotalRating() - 1);

            productRepository.save(product);
        };
    }
}
