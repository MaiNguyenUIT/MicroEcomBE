package com.example.rating_service.controller;

import com.example.rating_service.DTO.RatingDTO;
import com.example.rating_service.model.Rating;
import com.example.rating_service.service.RatingService;
import com.example.rating_service.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/rating")
public class RatingController {
    @Autowired
    private RatingService ratingService;

    @PostMapping()
    public ResponseEntity<List<Rating>> createRating(@RequestBody RatingDTO ratingDTO){
        return new ResponseEntity<>(ratingService.createRating(ratingDTO), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRating(@PathVariable String id){
        ratingService.deleteRating(id);
        return new ResponseEntity<>("Delete rating successfully", HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Rating>> getAllProductRating(@RequestParam String productId){
        return new ResponseEntity<>(ratingService.getAllRatingByProductId(productId), HttpStatus.OK);
    }

    @GetMapping("/byStar")
    public ResponseEntity<Page<Rating>> getRatingsByStar(@RequestParam String productId,
                                                         @RequestParam int ratingStar,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size){
        return new ResponseEntity<>(ratingService.getRatingByRatingStar(productId, ratingStar, page, size), HttpStatus.OK);
    }
}
