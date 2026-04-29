package com.shopmart.cart;

import com.shopmart.model.Product;
import com.shopmart.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CartService {

    private final ProductRepository productRepository;
    private final Map<String, Cart> carts = new ConcurrentHashMap<>();

    public CartService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Cart getCart(String sessionId) {
        return carts.computeIfAbsent(sessionId, Cart::new);
    }

    public Cart addItem(String sessionId, Long productId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        Cart cart = getCart(sessionId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
        } else {
            cart.getItems().add(new CartItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice().doubleValue(),
                    quantity,
                    product.getImageUrl()
            ));
        }
        return cart;
    }

    public Cart updateItem(String sessionId, Long productId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        Cart cart = getCart(sessionId);
        cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .ifPresent(i -> i.setQuantity(quantity));
        return cart;
    }

    public Cart removeItem(String sessionId, Long productId) {
        Cart cart = getCart(sessionId);
        cart.getItems().removeIf(i -> i.getProductId().equals(productId));
        return cart;
    }

    public void clearCart(String sessionId) {
        carts.remove(sessionId);
    }
}
