package com.example.cache.redis.repository;

import com.example.cache.redis.model.CacheConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CacheConfigRepository extends MongoRepository<CacheConfig,String> {
}
