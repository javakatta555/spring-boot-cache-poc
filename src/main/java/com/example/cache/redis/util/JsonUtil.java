package com.example.cache.redis.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    private static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    private JsonUtil() { }

    /**
     * Convenience method for converting any object to Json
     */
    public static JsonNode toJson(Object objectToConvert) {
        return objectMapper.convertValue(objectToConvert, JsonNode.class);
    }

    public static <T> T fromJson(JsonNode jsonNode, Class<T> clazz) {
        return objectMapper.convertValue(jsonNode, clazz);
    }

    public static <T> T fromJsonString(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.convertValue(objectMapper.readTree(jsonString), clazz);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static <T> T fromJsonString(String jsonString, TypeReference<T> clazz) {
        try {
            return objectMapper.convertValue(objectMapper.readTree(jsonString), clazz);
        } catch (Exception e) {
            return null;
        }
    }

    public static String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static <T> T convert(Object object, Class<T> clazz) {
        return fromJson(toJson(object), clazz);
    }

    /**
     * Removes all those key-value pairs whose values are null.
     */
    public static void cleanNulls(ObjectNode objectNode) {
        List<String> nullKeys = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> iterator = objectNode.fields();
        iterator.forEachRemaining(entry -> {
            if (entry.getValue() == null || entry.getValue().isNull())
                nullKeys.add(entry.getKey());
        });

        for (String key: nullKeys)
            objectNode.remove(key);
    }
}
