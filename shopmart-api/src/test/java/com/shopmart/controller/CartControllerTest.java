package com.shopmart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopmart.dto.CartItemRequest;
import com.shopmart.model.entity.CartItem;
import com.shopmart.model.entity.Product;
import com.shopmart.model.entity.User;
import com.shopmart.model.enums.Role;
import com.shopmart.repository.CartItemRepository;
import com.shopmart.repository.ProductRepository;
import com.shopmart.repository.UserRepository;
import com.shopmart.security.JwtTokenProvider;
import com.shopmart.testsupport.AuthTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User customer;
    private User otherCustomer;
    private Product product;
    private String customerAuthHeader;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        customer = userRepository.save(User.builder()
                .email("cart-customer@shopmart.com")
                .password(passwordEncoder.encode("password1"))
                .role(Role.CUSTOMER)
                .build());
        otherCustomer = userRepository.save(User.builder()
                .email("cart-other@shopmart.com")
                .password(passwordEncoder.encode("password1"))
                .role(Role.CUSTOMER)
                .build());
        product = productRepository.save(Product.builder()
                .name("Bluetooth Speaker")
                .description("Portable speaker")
                .price(new BigDecimal("39.99"))
                .stockQuantity(10)
                .category("Electronics")
                .active(true)
                .build());

        customerAuthHeader = AuthTestUtils.bearer(jwtTokenProvider, customer);
    }

    @Test
    void addItem_newItem_returns201Created() throws Exception {
        // BUG (intentional, CartController#addItem): a newly created cart line currently
        // returns 200 OK instead of 201 Created. This test asserts the CORRECT behavior
        // and is expected to fail against the buggy implementation.
        CartItemRequest request = new CartItemRequest(product.getId(), 2);

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", customerAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId", is(product.getId().intValue())))
                .andExpect(jsonPath("$.quantity", is(2)));
    }

    @Test
    void addItem_quantityExceedsStock_returns400() throws Exception {
        CartItemRequest request = new CartItemRequest(product.getId(), 999);

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", customerAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_productNotFound_returns404() throws Exception {
        CartItemRequest request = new CartItemRequest(999_999L, 1);

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", customerAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addItem_withoutAuthentication_returns401() throws Exception {
        CartItemRequest request = new CartItemRequest(product.getId(), 1);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCart_returnsItemsAndTotals() throws Exception {
        cartItemRepository.save(CartItem.builder().user(customer).product(product).quantity(2).build());

        mockMvc.perform(get("/api/cart").header("Authorization", customerAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.subtotal", is(79.98)));
    }

    @Test
    void removeItem_ownedItem_returns204() throws Exception {
        CartItem item = cartItemRepository.save(CartItem.builder().user(customer).product(product).quantity(1).build());

        mockMvc.perform(delete("/api/cart/items/" + item.getId()).header("Authorization", customerAuthHeader))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeItem_belongsToAnotherUser_returns403() throws Exception {
        CartItem item = cartItemRepository.save(CartItem.builder().user(otherCustomer).product(product).quantity(1).build());

        mockMvc.perform(delete("/api/cart/items/" + item.getId()).header("Authorization", customerAuthHeader))
                .andExpect(status().isForbidden());
    }
}
