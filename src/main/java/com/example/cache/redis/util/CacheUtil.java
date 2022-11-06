package com.example.cache.redis.util;

public class CacheUtil {
    private CacheUtil() {}

    public static String getCacheMapName(String databaseName, String className) {
        return String.format("%s_%s", databaseName, className);
    }
}
