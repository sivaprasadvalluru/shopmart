package com.shopmart;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getEmptyCart() throws Exception {
        mockMvc.perform(get("/api/cart")
                        .header("X-Session-Id", "test-empty-session")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void addItemToCart() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .header("X-Session-Id", "test-add-session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\": 1, \"quantity\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity", is(2)));
    }

    @Test
    void updateCartItemQuantity() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .header("X-Session-Id", "test-update-session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\": 1, \"quantity\": 1}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/cart/items/1")
                        .header("X-Session-Id", "test-update-session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity", is(5)));
    }

    @Test
    void removeCartItem() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .header("X-Session-Id", "test-remove-session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\": 1, \"quantity\": 1}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/cart/items/1")
                        .header("X-Session-Id", "test-remove-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void addItemWithZeroQuantityReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .header("X-Session-Id", "test-zero-qty-session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\": 1, \"quantity\": 0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItemWithNegativeQuantityReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .header("X-Session-Id", "test-neg-qty-session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\": 1, \"quantity\": -3}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItemReturnsNonEmptyItemsArray() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .header("X-Session-Id", "test-nonempty-session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\": 1, \"quantity\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", not(empty())));
    }

    @Test
    void cartWithoutSessionHeaderUsesDefault() throws Exception {
        mockMvc.perform(get("/api/cart").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", notNullValue()));
    }
}
