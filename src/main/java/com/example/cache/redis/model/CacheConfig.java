package com.example.cache.redis.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheConfig {

    // The database for which these configurations apply.
    private String database;
    // The list of entities/models for which caching is enabled
    private List<String> enabledEntities;
    // The map of custom cache settings for each of the enabled entities.
    private Map<String, LocalCachedMapConfig> configMap;
}
