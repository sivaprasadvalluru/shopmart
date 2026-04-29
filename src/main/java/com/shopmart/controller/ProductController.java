package com.shopmart.controller;

import com.shopmart.model.Product;
import com.shopmart.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<Product> listProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String name) {
        boolean hasCategory = categoryId != null;
        boolean hasName = name != null && !name.isBlank();
        if (hasCategory && hasName) {
            return productRepository.findByCategoryIdAndNameContainingIgnoreCase(categoryId, name);
        }
        if (hasCategory) {
            return productRepository.findByCategoryId(categoryId);
        }
        if (hasName) {
            return productRepository.findByNameContainingIgnoreCase(name);
        }
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
