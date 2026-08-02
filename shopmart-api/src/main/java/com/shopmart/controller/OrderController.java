package com.shopmart.controller;

import com.shopmart.dto.OrderResponse;
import com.shopmart.dto.OrderStatusUpdateRequest;
import com.shopmart.model.entity.User;
import com.shopmart.model.enums.Role;
import com.shopmart.repository.UserRepository;
import com.shopmart.security.UserPrincipal;
import com.shopmart.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@AuthenticationPrincipal UserPrincipal principal) {
        User user = currentUser(principal);
        OrderResponse response = orderService.placeOrder(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(@AuthenticationPrincipal UserPrincipal principal) {
        User user = currentUser(principal);
        return ResponseEntity.ok(orderService.getOrders(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        User user = currentUser(principal);
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        return ResponseEntity.ok(orderService.getOrderById(user, id, isAdmin));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(orderService.updateStatus(id, request.status()));
    }

    private User currentUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
    }
}
