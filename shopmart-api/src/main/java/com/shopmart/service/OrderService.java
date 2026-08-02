package com.shopmart.service;

import com.shopmart.dto.OrderItemResponse;
import com.shopmart.dto.OrderResponse;
import com.shopmart.exception.InvalidRequestException;
import com.shopmart.exception.ResourceNotFoundException;
import com.shopmart.model.entity.CartItem;
import com.shopmart.model.entity.Order;
import com.shopmart.model.entity.OrderItem;
import com.shopmart.model.entity.Product;
import com.shopmart.model.entity.User;
import com.shopmart.model.enums.OrderStatus;
import com.shopmart.repository.CartItemRepository;
import com.shopmart.repository.OrderRepository;
import com.shopmart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    // BUG (intentional): this method writes to both the orders/order_items tables and
    // the cart_items table but is not wrapped in @Transactional, so a failure partway
    // through (e.g. saving the order succeeds but clearing the cart fails) leaves the
    // cart and order history inconsistent instead of rolling back atomically.
    public OrderResponse placeOrder(User user) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new InvalidRequestException("Cannot place an order with an empty cart");
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();
            order.getItems().add(orderItem);

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        cartItemRepository.deleteByUser(user);

        return toResponse(saved);
    }

    public List<OrderResponse> getOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse getOrderById(User user, Long orderId, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!isAdmin && !order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Cannot view another user's order");
        }

        return toResponse(order);
    }

    // BUG (intentional): status transitions are not validated at all, so an order can
    // move from any status to any other status - including DELIVERED -> PENDING - with
    // no business-rule check on the transition being legal.
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPriceAtPurchase()
                ))
                .toList();

        return new OrderResponse(order.getId(), order.getStatus(), order.getTotalAmount(), order.getCreatedAt(), items);
    }
}
