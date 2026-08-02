package com.shopmart.service;

import com.shopmart.dto.CartItemRequest;
import com.shopmart.dto.CartItemResponse;
import com.shopmart.dto.CartResponse;
import com.shopmart.exception.InvalidRequestException;
import com.shopmart.exception.ResourceNotFoundException;
import com.shopmart.model.entity.CartItem;
import com.shopmart.model.entity.Product;
import com.shopmart.model.entity.User;
import com.shopmart.repository.CartItemRepository;
import com.shopmart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final BigDecimal LOYALTY_DISCOUNT_RATE = new BigDecimal("0.10");

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartItemResponse addItem(User user, CartItemRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.productId()));

        if (request.quantity() > product.getStockQuantity()) {
            throw new InvalidRequestException("Requested quantity exceeds available stock");
        }

        CartItem cartItem = cartItemRepository.findByUserAndProductId(user, product.getId())
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + request.quantity());
                    return existing;
                })
                .orElseGet(() -> CartItem.builder()
                        .user(user)
                        .product(product)
                        .quantity(request.quantity())
                        .build());

        CartItem saved = cartItemRepository.save(cartItem);
        return toResponse(saved);
    }

    public CartResponse getCart(User user) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        BigDecimal subtotal = cartItems.stream()
                .map(this::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = subtotal.multiply(LOYALTY_DISCOUNT_RATE);

        // BUG (intentional): the loyalty discount is subtracted twice, so grandTotal
        // ends up understated by an extra 10% of the subtotal.
        BigDecimal grandTotal = subtotal.subtract(discount).subtract(discount);

        List<CartItemResponse> items = cartItems.stream().map(this::toResponse).toList();

        return new CartResponse(items, subtotal, discount, grandTotal);
    }

    public void removeItem(User user, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Cannot remove another user's cart item");
        }

        cartItemRepository.delete(cartItem);
    }

    private BigDecimal lineTotal(CartItem item) {
        return item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    private CartItemResponse toResponse(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                lineTotal(item)
        );
    }
}
