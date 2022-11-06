package com.example.cache.redis.repository;

import com.example.cache.redis.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product,String> {
}
