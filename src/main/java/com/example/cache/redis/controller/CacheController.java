package com.example.cache.redis.controller;


import com.example.cache.redis.cache.RedisCache;
import com.example.cache.redis.cache.RedisCacheManager;
import com.example.cache.redis.config.CacheConfiguration;
import com.example.cache.redis.dto.ClearCacheRequest;
import com.example.cache.redis.model.CacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.List;

import static com.example.cache.redis.util.CacheUtil.getCacheMapName;


/**
 * Utility routes for manipulating/clearing cache
 *
 *
 * NOTE: The /cache/clear* routes clear the cache entry from BOTH Local instances and central Redis cache as well
 *       The /cache/refresh* routes clear cache ONLY from the local instances, leaving the central Redis cache intact
 *       The /cacheConfig/clear route clears the cacheConfigs from memory, forcing a re-read of the configs from DB
 */
@Controller
@Slf4j
public class CacheController {

    private static final String RMAP_CACHE = "RMapCache";
    @Autowired
    private CacheConfiguration cacheConfiguration;

    @Autowired
    private RedisCacheManager cacheManager;

    @Value("${cache.accessToken}")
    private String cacheAccessToken;

    private static final String ERROR_TOKEN_MISSING = "accessToken is missing";


    /**
     * Clear the cache for a specific set of entries of a particular CacheMap.
     * Clears from BOTH Local and Central Cache
     * Eg: Clear cache for specific partnerIds from partner_Partner
     */
    @PostMapping("/cache/clearSpecific")
    public ResponseEntity clearSpecificEntries(@RequestBody ClearCacheRequest clearCacheRequest,
                                               @RequestHeader(required = false) String accessToken) {
        if (!validateToken(accessToken)) {
            log.error(ERROR_TOKEN_MISSING);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String cacheMap = getCacheMapName(clearCacheRequest.getDatabase(), clearCacheRequest.getEntity());
        try {
            RedisCache cache = cacheManager.getCache(cacheMap);
            cache.evict(clearCacheRequest.getIds());
        } catch (Exception e) {
            log.error("Error in invalidating cache for cacheMap {} with ids {}", cacheMap, clearCacheRequest.getIds());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    /**
     * Clear all CacheMaps for a particular database from BOTH Local and Central Cache
     * Eg: Clear all product caches
     */
    @PostMapping("/cache/clearDatabase")
    public ResponseEntity clearAllCacheMapsForSingleDatabase(@RequestBody ClearCacheRequest clearCacheRequest,
                                                             @RequestHeader(required = false) String accessToken) {
        if (!validateToken(accessToken)) {
            log.error(ERROR_TOKEN_MISSING);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CacheConfig cacheConfig = cacheConfiguration.getCacheConfig(clearCacheRequest.getDatabase());
        if (cacheConfig != null) {
            for (String entity: cacheConfig.getEnabledEntities()) {
                String cacheName = getCacheMapName(clearCacheRequest.getDatabase(), entity);
                RedisCache cache = cacheManager.getCache(cacheName);
                cache.clear();
            }
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Clear all CacheMaps across all databases from BOTH Local and Central Cache
     * Eg: Clear caches across all brokers and collections.
     */
    @PostMapping("/cache/clearAll")
    public ResponseEntity clearAllCacheMapsAcrossAllDatabases(@RequestHeader(required = false) String accessToken) {
        if (!validateToken(accessToken)) {
            log.error(ERROR_TOKEN_MISSING);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<CacheConfig> cacheConfigs = cacheConfiguration.getAllCacheConfigs();
        for (CacheConfig cacheConfig: cacheConfigs) {
            for (String entity: cacheConfig.getEnabledEntities()) {
                String cacheName = getCacheMapName(cacheConfig.getDatabase(), entity);
                RedisCache cache = cacheManager.getCache(cacheName);
                cache.clear();
            }
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Clear all the CacheConfig settings from the in-memory map. This can be used when CacheConfigs are to be loaded
     * from the DB.
     *
     * For the local cache instances to use these settings and reconfigure themselves, you will need to
     * delete the local caches, so that they rebuild themselves with the new settings. You can either call any of the
     * /cache/clear* methods if you want to clear the central caches too along with local caches. If you want to
     * maintain the central caches, then call the /cache/refreshLocal* methods
     */
    @PostMapping("/cacheConfig/clear")
    public ResponseEntity clearConfigs(@RequestHeader(required = false) String accessToken) {
        if (!validateToken(accessToken)) {
            log.error(ERROR_TOKEN_MISSING);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        cacheConfiguration.clearConfigs();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Clear an entire CacheMap from Local Cache ONLY.
     * The local cache maps will be rebuilt using new configs, and will re-read cached values from central cache
     */
    @PostMapping("/cache/refreshMap")
    public ResponseEntity refreshSingleLocalCacheMap(@RequestBody ClearCacheRequest clearCacheRequest,
                                                     @RequestHeader(required = false) String accessToken) {
        if (!validateToken(accessToken)) {
            log.error(ERROR_TOKEN_MISSING);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String cacheName = getCacheMapName(clearCacheRequest.getDatabase(), clearCacheRequest.getEntity());
        cacheManager.deleteLocalCache(cacheName);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Clear all CacheMaps for a particular database from Local Cache ONLY.
     * The local cache maps will be rebuilt using new configs, and will re-read cached values from central cache
     */
    @PostMapping("/cache/refreshDatabase")
    public ResponseEntity refreshAllLocalCacheMapsForSingleDatabase(@RequestBody ClearCacheRequest clearCacheRequest,
                                                                    @RequestHeader(required = false) String accessToken) {
        if (!validateToken(accessToken)) {
            log.error(ERROR_TOKEN_MISSING);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CacheConfig cacheConfig = cacheConfiguration.getCacheConfig(clearCacheRequest.getDatabase());
        if (cacheConfig != null) {
            for (String entity: cacheConfig.getEnabledEntities()) {
                String cacheName = getCacheMapName(clearCacheRequest.getDatabase(), entity);
                cacheManager.deleteLocalCache(cacheName);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Clear all CacheMaps across all databases from Local Cache ONLY.
     * The local cache maps will be rebuilt using new configs, and will re-read cached values from central cache
     */
    @PostMapping("/cache/refreshAll")
    public ResponseEntity refreshAllCacheMapsAcrossAllDatabases(@RequestHeader(required = false) String accessToken) {
        if (!validateToken(accessToken)) {
            log.error(ERROR_TOKEN_MISSING);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<CacheConfig> cacheConfigs = cacheConfiguration.getAllCacheConfigs();
        for (CacheConfig cacheConfig: cacheConfigs) {
            for (String entity: cacheConfig.getEnabledEntities()) {
                String cacheName = getCacheMapName(cacheConfig.getDatabase(), entity);
                cacheManager.deleteLocalCache(cacheName);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * API to clear the failed cache for a specific set of entries of a particular CacheMap.
     * Will run periodically to clear out the invalid cache.
     * Clears from BOTH Local and Central Cache
     *
     * Fetches invalid cache entries from db and then clears them. Scheduled for every hour.
     */
    @Scheduled(cron = "0 0 * * * ?") //scheduled for every hour
    @GetMapping("/cache/clearInvalidated")
    public ResponseEntity clearFailedInvalidatedCache() {
        log.info("Scheduling cache clear!!!");
        //cacheCleaner.clearInvalidCache();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Common method to check that the accessToken is present in the request header
     */
    private boolean validateToken(String accessToken) {
        return (cacheAccessToken.equalsIgnoreCase(accessToken));
    }

    /*
     Returns the cache hits/misses/put performance metrics for all currently existing caches
    @GetMapping("/cache/performance")
    public ResponseEntity getPerformanceMetrics(@RequestHeader(required = false) String accessToken) {
        if (!validateToken(accessToken)) {
            log.error(ERROR_TOKEN_MISSING);
            return forbidden();
        }

        List<String> cacheNames = cacheManager.getCacheNames();
        List<CacheMetrics> metrics = new ArrayList<>();
        for (String cacheName: cacheNames) {
            RedisCache cache = cacheManager.getCache(cacheName);
            metrics.add(new CacheMetrics(cache));
        }
        return ok(metrics);
    }
    */

}
