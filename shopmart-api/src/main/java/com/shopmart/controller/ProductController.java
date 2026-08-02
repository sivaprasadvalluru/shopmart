package com.shopmart.controller;

import com.shopmart.dto.ProductDto;
import com.shopmart.model.entity.Product;
import com.shopmart.repository.ProductRepository;
import com.shopmart.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<Page<ProductDto>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category
    ) {
        // BUG (intentional): should be PageRequest.of(page, size) - adding 1 here shifts
        // every request forward by one page, so page=0 skips the first page of results.
        Pageable pageable = PageRequest.of(page + 1, size);
        return ResponseEntity.ok(productService.getProducts(pageable, category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        // BUG (intentional): should use productRepository.findById(id)
        // .orElseThrow(() -> new ResourceNotFoundException(...)) like ProductService does.
        // Calling .get() directly throws an unhandled NoSuchElementException for a
        // missing product instead of a clean 404.
        Product product = productRepository.findById(id).get();
        return ResponseEntity.ok(ProductDto.from(product));
    }
}
