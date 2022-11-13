package com.example.cache.aerospike.repository;

import com.example.cache.aerospike.model.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RatingRepository extends MongoRepository<Rating,String> {
}
