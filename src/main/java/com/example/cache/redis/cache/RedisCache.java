package com.example.cache.redis.cache;

import com.example.cache.redis.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLocalCachedMap;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
@Slf4j
public class RedisCache implements Cache {

    /**
     * A RLocalCachedMap is a special map data structure provided by Redis.
     * It is basically just a local in-memory hashMap (with one special property) that contains a copy of the central
     * cached data. Each java instance will have and maintain it's own local hashMap. When a hit comes in, the local
     * hashMap is checked first, and then if the data is not found, central Redis cache is checked.
     *
     * The special property of these RLocalCachedMap instances is that they seamlessly sync themselves with the central
     * redis cache whenever data has to be fetched/cached, while still providing the simple interface of a regular
     * hashMap.
     *
     * All actions pertaining to keeping RLocalCachedMap in sync with Central Cache are handled under the hood. The
     * sync logic can be customized by providing relevant options at the time the RLocalCachedMap instances are created.
     */
    private RLocalCachedMap<Object, Object> map;

    // The name of the cache. (Will generally be of the form 'database_entity'. Eg: turtlemint_Partner, ippb_Lead, etc)
    private String name;

    // The type of data being stored in this cache.
    // This is used to deserialize the cached data back to it's original type
    private Class classType;

    // For performance tracking
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();
    // The instant of time when this cache was created. Will help make better sense of the performance metrics


    public RedisCache(String name, RLocalCachedMap<Object, Object> map, Class classType) {
        this.name = name;
        this.map = map;
        this.classType = classType;
    }

    @Override
    @NonNull
    public String getName() {
        return name;
    }

    @Override
    @NonNull
    public RLocalCachedMap<Object, Object> getNativeCache() {
        return map;
    }

    @Override
    public ValueWrapper get(@NonNull Object key) {
        Object value = map.get(key);
        if (value == null) {
            log.info("Entry for key {} not found in {} cache", key, name);
            return null;
        }
        Object cachedObject = JsonUtil.fromJsonString((String) value, classType);
        if (cachedObject == null) {
            log.error("Failed to deserialize object! {}: {}: {}", value, key, classType.getName());
            return null;
        }
        log.info("Returning cached value for entry with key {} from {} cache", key, name);
        return new SimpleValueWrapper(cachedObject);
    }

    @Override
    public <T> T get(@NonNull Object key, Class<T> type) {
        // This method doesn't seem to be used by Spring cache
        // We will throw an error just in-case, so that we know if this method gets erroneously called.
        log.error("Unexpected get(Object key, Class<T> type) method of RedisCache called!");
        return null;
    }

    @Override
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        // This method doesn't seem to be used by Spring cache
        // We will throw an error just in-case, so that we know if this method gets erroneously called.
        log.error("Unexpected get(Object key, Callable<T> valueLoader) method of RedisCache called!");
        return null;
    }

    @Override
    public void put(@NonNull Object key, Object value) {
        log.info("Inserting entry for key:{} and value:{} into {} cache", key, JsonUtil.toJsonString(value), name);
        map.fastPut(key, JsonUtil.toJsonString(value));
    }

    @Override
    public void evict(@NonNull Object key) {
        log.info("Removing entry for key:{} from {} cache", key, name);
        map.fastRemove(key);
    }

    /**
     * Evict a list of keys from cache
     */
    public void evict(@NonNull List<Object> keys) {
        log.info("Removing entries for keys:{} from {} cache", JsonUtil.toJsonString(keys), name);
        map.fastRemove(keys.toArray(new Object[0]));
    }

    /**
     * Evict all keys from cache
     */
    @Override
    public void clear() {
        log.info("Clearing all entries for {} cache", name);
        map.clear();
    }

    /**
     * Clears ONLY the local in-memory CacheMap of all java instances.
     *
     * This can be useful when you have manually manipulated the data in central cache and want to force the all your
     * java instances to dump their local caches and re-read the new data from central cache.
     */
    public void clearLocalDataOnly() {
        log.info("Clearing all entries for {} cache from local cacheMap only", name);
        map.clearLocalCache();
    }
}
