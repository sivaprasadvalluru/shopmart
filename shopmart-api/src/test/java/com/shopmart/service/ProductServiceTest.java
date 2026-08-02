package com.shopmart.service;

import com.shopmart.dto.ProductDto;
import com.shopmart.exception.ResourceNotFoundException;
import com.shopmart.model.entity.Product;
import com.shopmart.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = Product.builder()
                .id(1L)
                .name("Wireless Headphones")
                .description("Noise cancelling")
                .price(new BigDecimal("149.99"))
                .stockQuantity(42)
                .category("Electronics")
                .active(true)
                .build();

        product2 = Product.builder()
                .id(2L)
                .name("Denim Jacket")
                .description("Slim fit")
                .price(new BigDecimal("59.99"))
                .stockQuantity(35)
                .category("Clothing")
                .active(true)
                .build();
    }

    @Test
    void getProducts_withoutCategory_delegatesToFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product1, product2), pageable, 2);
        when(productRepository.findAll(pageable)).thenReturn(page);

        Page<ProductDto> result = productService.getProducts(pageable, null);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(ProductDto::id).containsExactly(1L, 2L);
        verify(productRepository).findAll(pageable);
        verify(productRepository, never()).findByCategoryIgnoreCase(anyString(), any());
    }

    @Test
    void getProducts_withBlankCategory_delegatesToFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product1, product2), pageable, 2);
        when(productRepository.findAll(pageable)).thenReturn(page);

        productService.getProducts(pageable, "   ");

        verify(productRepository).findAll(pageable);
        verify(productRepository, never()).findByCategoryIgnoreCase(anyString(), any());
    }

    @Test
    void getProducts_withCategory_delegatesToFindByCategoryIgnoreCase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product2), pageable, 1);
        when(productRepository.findByCategoryIgnoreCase(eq("Clothing"), eq(pageable))).thenReturn(page);

        Page<ProductDto> result = productService.getProducts(pageable, "Clothing");

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Denim Jacket");
        verify(productRepository, times(1)).findByCategoryIgnoreCase("Clothing", pageable);
        verify(productRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getProductById_found_returnsDto() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        ProductDto dto = productService.getProductById(1L);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Wireless Headphones");
        assertThat(dto.price()).isEqualByComparingTo("149.99");
    }

    @Test
    void getProductById_notFound_throwsResourceNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
