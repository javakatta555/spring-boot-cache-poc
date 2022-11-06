package com.example.cache.redis.dto;

import lombok.Data;

import java.util.List;

@Data
public class ClearCacheRequest {

    private String database;
    private String entity;
    private List<Object> ids;
}
