package com.shopmart.controller;

import com.shopmart.cart.Cart;
import com.shopmart.cart.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final String DEFAULT_SESSION = "default";

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    private String resolveSession(String sessionId) {
        return (sessionId != null && !sessionId.isBlank()) ? sessionId : DEFAULT_SESSION;
    }

    @GetMapping
    public Cart getCart(@RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        return cartService.getCart(resolveSession(sessionId));
    }

    @PostMapping("/items")
    public Cart addItem(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        // Intentional course defect: quantity accepted as-is with no validation
        int quantity = Integer.parseInt(body.get("quantity").toString());
        return cartService.addItem(resolveSession(sessionId), productId, quantity);
    }

    @PutMapping("/items/{productId}")
    public Cart updateItem(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @PathVariable Long productId,
            @RequestBody Map<String, Object> body) {
        // Intentional course defect: quantity accepted as-is with no validation
        int quantity = Integer.parseInt(body.get("quantity").toString());
        return cartService.updateItem(resolveSession(sessionId), productId, quantity);
    }

    @DeleteMapping("/items/{productId}")
    public Cart removeItem(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @PathVariable Long productId) {
        return cartService.removeItem(resolveSession(sessionId), productId);
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> checkout(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        Cart cart = cartService.getCart(resolveSession(sessionId));
        if (cart.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot checkout with an empty cart"));
        }
        cartService.clearCart(resolveSession(sessionId));
        return ResponseEntity.ok(Map.of("message", "Order placed successfully"));
    }
}
