package com.shopmart.controller;

import com.shopmart.dto.CartItemRequest;
import com.shopmart.dto.CartItemResponse;
import com.shopmart.dto.CartResponse;
import com.shopmart.model.entity.User;
import com.shopmart.repository.UserRepository;
import com.shopmart.security.UserPrincipal;
import com.shopmart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> addItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CartItemRequest request
    ) {
        User user = currentUser(principal);
        CartItemResponse response = cartService.addItem(user, request);
        // BUG (intentional): a newly created cart line should return 201 Created,
        // not 200 OK.
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        User user = currentUser(principal);
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> removeItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        User user = currentUser(principal);
        cartService.removeItem(user, id);
        return ResponseEntity.noContent().build();
    }

    private User currentUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
    }
}
