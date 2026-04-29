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
        // Intentional course defect: quantity is not validated — 0 or negative values are accepted
        Cart cart = getCart(sessionId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst();

        if (existing.isPresent()) {
            // Intentional course defect: merging quantities without bounds check
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
        // Intentional course defect: quantity is not validated — 0 or negative values are accepted
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
}
