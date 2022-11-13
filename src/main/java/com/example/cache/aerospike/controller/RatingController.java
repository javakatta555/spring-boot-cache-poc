package com.example.cache.aerospike.controller;

import com.example.cache.aerospike.model.Rating;
import com.example.cache.aerospike.repository.RatingRepository;
import com.example.cache.aerospike.services.AerospikeCacheServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@Slf4j
public class RatingController {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private AerospikeCacheServiceImpl aerospikeCacheService;

    @GetMapping(value = "/rating/{id}")
    public Rating getRating(@PathVariable String id){
        Rating rating = (Rating) aerospikeCacheService.get(id,"rating",id,"config",new TypeReference<Rating>(){});
        log.info("Fetch rating from cache");
        if(rating == null){
            log.info("Fetch rating from db");
            rating= ratingRepository.findById(id).get();
            aerospikeCacheService.put(id,"rating",id,"config",rating);
        }
        return rating;
    }

    @PostMapping("/rating")
    public Rating saveRating(@RequestBody Rating rating){
        ratingRepository.save(rating);
        return rating;
    }

}
