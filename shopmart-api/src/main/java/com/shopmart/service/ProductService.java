package com.shopmart.service;

import com.shopmart.dto.ProductDto;
import com.shopmart.exception.ResourceNotFoundException;
import com.shopmart.model.entity.Product;
import com.shopmart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Page<ProductDto> getProducts(Pageable pageable, String category) {
        Page<Product> products = StringUtils.hasText(category)
                ? productRepository.findByCategoryIgnoreCase(category, pageable)
                : productRepository.findAll(pageable);
        return products.map(ProductDto::from);
    }

    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return ProductDto.from(product);
    }
}
