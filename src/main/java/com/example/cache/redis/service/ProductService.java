package com.example.cache.redis.service;

import com.example.cache.redis.cache.RedisCache;
import com.example.cache.redis.cache.RedisCacheManager;
import com.example.cache.redis.model.Product;
import com.example.cache.redis.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RedisCacheManager cacheManager;

    @Cacheable(cacheNames = "product")
    public List<Product> getAllProduct(){
        return productRepository.findAll();
    }

    @Cacheable(cacheNames = "product",key = "#id")
    public Optional<Product> findProductById(String id){
        RedisCache redisCache = null;
        Product product = null;
        try {
            redisCache = cacheManager.getCache("product");
            Cache.ValueWrapper data = redisCache.get(id);
            if (Objects.nonNull(data) && Objects.nonNull(data.get()))
                return (Optional<Product>) data.get();
        } catch (Exception e) {
            log.error("[findProductById] Error fetching product from cache for {}", "product", e);
            if (Objects.nonNull(redisCache))
                redisCache.evict(id);
        }
        product = productRepository.findById(id).orElse(null);
        if(Objects.nonNull(redisCache) && Objects.nonNull(product)){
            log.info("Fetched from db and adding in cache for future reference");
            redisCache.put(product.getId(),product);
        }
        return Optional.ofNullable(product);
    }

    @CachePut(cacheNames = "product",key = "#product.id")
    public Product saveProduct(Product product){
        productRepository.save(product);
        return product;
    }

    @CacheEvict(cacheNames = "product",key = "#id")
    public String deleteProduct(String id){
        productRepository.deleteById(id);
        return id;
    }


}
