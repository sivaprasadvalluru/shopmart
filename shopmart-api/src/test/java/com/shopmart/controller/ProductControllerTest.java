package com.shopmart.controller;

import com.shopmart.model.entity.Product;
import com.shopmart.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    private List<Product> seededInOrder;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        for (int i = 1; i <= 25; i++) {
            productRepository.save(Product.builder()
                    .name("Test Product " + i)
                    .description("Description " + i)
                    .price(new BigDecimal("9.99").add(BigDecimal.valueOf(i)))
                    .stockQuantity(100)
                    .category("TestCategory")
                    .active(true)
                    .build());
        }
        seededInOrder = productRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    void getProducts_firstPage_returnsFirstPageOfResults() throws Exception {
        // BUG (intentional, ProductController#getProducts): PageRequest.of(page + 1, size)
        // shifts every request forward by one page, so page=0 currently skips the first
        // page of results. This test asserts the CORRECT behavior (page=0 returns the
        // first `size` products) and is expected to fail against the buggy implementation.
        Long expectedFirstId = seededInOrder.get(0).getId();
        String expectedFirstName = seededInOrder.get(0).getName();

        mockMvc.perform(get("/api/products").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(10)))
                .andExpect(jsonPath("$.content[0].id", is(expectedFirstId.intValue())))
                .andExpect(jsonPath("$.content[0].name", is(expectedFirstName)));
    }

    @Test
    void getProducts_totalElementsReflectsAllSeededProducts() throws Exception {
        mockMvc.perform(get("/api/products").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(25)));
    }

    @Test
    void getProducts_filtersByCategory() throws Exception {
        // Note: totalElements comes from a COUNT query and content-item category membership
        // is independent of which page is actually served, so this assertion is unaffected by
        // the ProductController pagination bug (tested separately above) - it exercises only
        // the category-filtering behavior.
        for (int i = 1; i <= 10; i++) {
            productRepository.save(Product.builder()
                    .name("Clothing Item " + i)
                    .description("desc")
                    .price(new BigDecimal("15.00"))
                    .stockQuantity(5)
                    .category("Clothing")
                    .active(true)
                    .build());
        }

        mockMvc.perform(get("/api/products").param("page", "0").param("size", "5").param("category", "Clothing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(10)))
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.not(org.hamcrest.Matchers.empty())))
                .andExpect(jsonPath("$.content[0].category", is("Clothing")));
    }

    @Test
    void getProductById_existingProduct_returnsProduct() throws Exception {
        Product product = seededInOrder.get(0);

        mockMvc.perform(get("/api/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(product.getId().intValue())))
                .andExpect(jsonPath("$.name", is(product.getName())));
    }

    @Test
    void getProductById_nonexistentProduct_returns404() throws Exception {
        // BUG (intentional, ProductController#getProductById): calling .get() directly on
        // the Optional throws an unhandled NoSuchElementException for a missing product,
        // which the generic exception handler maps to 500 instead of a clean 404. This test
        // asserts the CORRECT behavior and is expected to fail against the buggy implementation.
        long nonexistentId = 999_999L;

        mockMvc.perform(get("/api/products/" + nonexistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProducts_isAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }
}
