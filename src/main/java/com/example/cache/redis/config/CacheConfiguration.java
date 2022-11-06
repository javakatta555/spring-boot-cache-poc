package com.example.cache.redis.config;

import com.example.cache.redis.model.CacheConfig;
import com.example.cache.redis.model.LocalCachedMapConfig;
import com.example.cache.redis.repository.CacheConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
public class CacheConfiguration {

    @Autowired
    private CacheConfigRepository cacheConfigRepository;

    private Map<String, CacheConfig> cacheConfigMap = new HashMap<>();

    /**
     * Returns the CacheConfig for a particular database.
     * Returns from the in-memory hash-map if it is present, else fetches the config from DB and stores it in hash-map
     * for future retrievals.
     */
    public CacheConfig getCacheConfig(String database) {
        if (cacheConfigMap.containsKey(database)) {
            return cacheConfigMap.get(database);
        }
        else {
            try {
                log.info("CacheConfig for {} doesn't exist in the in-memory map. Fetching from DB", database);
                CacheConfig cacheConfig = cacheConfigRepository.findById(database).get();
                cacheConfigMap.put(database, cacheConfig);
                return cacheConfig;
            } catch (Exception e) {
                log.error("Failed to get cache config from DB", e);
                return null;
            }
        }
    }

    /**
     * Returns all the CacheConfigs from DB.
     */
    public List<CacheConfig> getAllCacheConfigs() {
        return cacheConfigRepository.findAll();
    }

    public LocalCachedMapConfig getLocalCachedMapConfig(String mapType) {
        if (mapType.contains("_")) {
            List<String> parts = Arrays.asList(mapType.split("_", 2));
            String database = parts.get(0);
            String entity = parts.get(1);
            CacheConfig cacheConfig = getCacheConfig(database);
            try {
                return cacheConfig.getConfigMap().get(entity);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }


    public void clearConfigs() {
        log.info("Clearing all CacheConfigs from in-memory map");
        cacheConfigMap.clear();
    }

}
