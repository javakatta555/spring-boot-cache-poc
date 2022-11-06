package com.example.cache.redis.controller;

import com.example.cache.redis.model.Product;
import com.example.cache.redis.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping(value = "/product")
    public List<Product> getAllProducts(){
        return productService.getAllProduct();
    }

    @GetMapping(value = "/product/{id}")
    public Optional<Product> findProductById(@PathVariable String id){
        return productService.findProductById(id);
    }

    @PostMapping(value = "/product")
    public Product saveProduct(@RequestBody Product product){
        return productService.saveProduct(product);
    }

    @DeleteMapping (value = "/product/{id}")
    public String deleteProductById(@PathVariable String id){
        return productService.deleteProduct(id);
    }
}
