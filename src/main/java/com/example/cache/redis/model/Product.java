package com.example.cache.redis.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product implements Serializable {

    @Id
    private String id;
    private String productCategory;
    private String productName;
}
