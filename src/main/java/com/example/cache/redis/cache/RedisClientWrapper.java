package com.example.cache.redis.cache;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RedisClientWrapper {

    @Autowired(required = false)
    private RedissonClient client;

    /**
     * Get or create a localCachedMap with specified name and options.
     *
     * A LocalCachedMap is just a local in-memory hashMap that contains a copy of the central cached data.
     * Each java instance will have and maintain it's own local hashMap. When a hit comes in, the local hashMap is
     * checked first, and then if the data is not found, central Redis cache is checked. All hashMaps across java
     * instances are automatically synced by redis.
     *
     * All actions pertaining to keeping LocalCacheMaps in sync with Central Cache are handled under the hood.
     */
    public RLocalCachedMap<Object, Object> getLocalCachedMap(String name, LocalCachedMapOptions<Object, Object> options) {
        return client.getLocalCachedMap(name, options);
    }

    public RMapCache<Object, Object> getRMapCache(String name, LocalCachedMapOptions<Object, Object> options) {
        return client.getMapCache(name, options);
    }

}

