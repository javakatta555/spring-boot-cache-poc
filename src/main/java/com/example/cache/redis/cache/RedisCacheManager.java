package com.example.cache.redis.cache;

import com.example.cache.redis.config.CacheConfiguration;
import com.example.cache.redis.model.LocalCachedMapConfig;
import com.example.cache.redis.model.Product;
import com.example.cache.redis.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;

@Component
@Slf4j
public class RedisCacheManager implements CacheManager {

    // A map to store the existing cache instances
    private static HashMap<String, RedisCache> caches = new HashMap<>();
    private static final Integer DEFAULT_CACHE_SIZE = 100;

    @Autowired
    private CacheConfiguration cacheConfiguration;

    /**
     * This wrapper over Redis allows us to connect to and interact with the underlying redis cache and procure
     * LocalCachedMaps (the underlying data structure of our cache Implementation)
     */
    @Autowired
    private RedisClientWrapper wrapper;


    @Override
    public RedisCache getCache(String name) {
        RedisCache cache;
        if (caches.containsKey(name))
            cache = caches.get(name);
        else {
            LocalCachedMapOptions<Object, Object> options = getOptions(name, DEFAULT_CACHE_SIZE);
            log.info("Cache for {} does not exist. Creating new Cache with config: {}",
                    name, JsonUtil.toJsonString(options));
            RLocalCachedMap<Object, Object> redisCacheMap = wrapper.getLocalCachedMap(name, options);
            Class classType = getClassTypeForEntity(name);
            cache = new RedisCache(name, redisCacheMap, classType);
            caches.put(name, cache);
        }
        return cache;
    }

    /**
     * Gets the custom config options for the cache by name. These config options
     * are fetched using the cacheConfiguration module. For now, cacheSize can be customized.
     */
    public LocalCachedMapOptions<Object, Object> getOptions(String cacheName, Integer cacheSize) {
        // Check whether there are any custom config options for this mapType.
        LocalCachedMapConfig config = cacheConfiguration.getLocalCachedMapConfig(cacheName);
        if (config != null) {
            // If config is present, override defaults
            cacheSize = config.getCacheSize();
        }
        return LocalCachedMapOptions.defaults()
                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
                .cacheSize(cacheSize);
    }
    @Override
    public Collection<String> getCacheNames() {
        return null;
    }

    private Class getClassTypeForEntity(String entityName) {
        switch (entityName) {
            case "product":
                return Product.class;
            default:
                return null;
        }
    }


    /**
     * Delete a specific local cache instance.
     *
     * This method would be useful when you have changed the cache configs and want to reinitialize the local cache
     * instances to use the new configs, while still leaving the central Cached data intact.
     *
     * Calling this method should only delete the local cache on all java instances. At the next 'get'/'put' call
     * the local cache will get recreated with the new configs. If it is a 'get' call, it will even check central cache
     * for the required data before hitting DB.
     *
     * TODO - As of now this method does not work as it should.
     * Current Behavior: This method will only delete the local cache instance from the current java instance only,
     * Expected Behavior: This method should delete the local cache instance from all of the java instances.
     *
     * Note on Clear vs Delete:
     *      Clear local cache = Clearing all entries from the local cache instance
     *      Delete local cache = Delete the local cache instance itself
     *                          (forcing it to be recreated with the new configs)
     *
     * Currently, even though this method will clear the local cache across all instances, it will not delete them and
     * force them to be recreated with new configs across all instances. This is because the clearing of the cacheMap
     * is handled under the hood by redis and synced across all instances, whereas the removal/deletion of the cacheMap
     * entirely from 'caches' is an action on a regular HashMap, and so this would not be synced across all instances.
     *
     * Will need additional research into how this can be achieved. However, this is of very low priority as it
     * has rarely been used.
     */
    public void deleteLocalCache(@NonNull String name) {
        RedisCache cache = getCache(name);
        log.info("Deleting Local CacheMap {}", name);
        cache.clearLocalDataOnly();
        caches.remove(name);
    }
}
