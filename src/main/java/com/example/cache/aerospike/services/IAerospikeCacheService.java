package com.example.cache.aerospike.services;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

public interface IAerospikeCacheService {

    /**
     * Get the data from cache from given set for a given key and given return object
     *
     * @param set
     * @param key
     * @param typeReference
     * @return
     */
    Object get(String broker, String set, String key, TypeReference<? extends Object> typeReference);

    /**
     * Get the data from cache from given set, given key and bin - returns Object
     *
     * @param set
     * @param key
     * @param bin
     * @return
     */
    Object get(String broker, String set, String key, String bin);

    /**
     * Delete the key and record from given set.
     *
     * @param set
     * @param key
     * @return
     */
    boolean delete(String broker,String set, String key);

    /**
     * Get the data from cache from given set, given key and bin - returns passed Object
     *
     * @param set
     * @param key
     * @param bin
     * @param typeReference
     * @return
     */
    Object get(String id,String set, String key, String bin, TypeReference typeReference);

    /**
     * Put the data map to cache for given set, given key and bin
     *
     * @param set
     * @param key
     * @param dataMap
     */
    void put(String broker,String set, String key, Map<String, ? extends Object> dataMap);

    /**
     * Put the data(Object) to cache for given set, given key and bin
     *
     * @param set
     * @param key
     * @param binName
     * @param value
     */
    void put(String broker,String set, String key, String binName, Object value);

    /**
     * Put the data to cache for given set, given key and bin for custom ttl
     *
     * @param set
     * @param ttl
     * @param key
     * @param binName
     * @param value
     */
    void put(String broker,String set, int ttl, String key, String binName, Object value);

    /**
     * Put the data map to cache for given set, given key and bin with custom ttl
     *
     * @param set
     * @param key
     * @param dataMap
     * @param ttl
     * @return
     */
    boolean put(String broker,String set, String key, Map<String, ? extends Object> dataMap, int ttl);

    /**
     * Put the String to cache for given set, given key and bin
     *
     * @param set
     * @param ttl
     * @param key
     * @param binName
     * @param value
     */
    void put(String broker,String set, int ttl, String key, String binName, String value);
}
