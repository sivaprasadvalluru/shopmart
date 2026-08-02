package com.shopmart.service;

import com.shopmart.dto.OrderResponse;
import com.shopmart.exception.InvalidRequestException;
import com.shopmart.exception.ResourceNotFoundException;
import com.shopmart.model.entity.CartItem;
import com.shopmart.model.entity.Order;
import com.shopmart.model.entity.Product;
import com.shopmart.model.entity.User;
import com.shopmart.model.enums.OrderStatus;
import com.shopmart.model.enums.Role;
import com.shopmart.repository.CartItemRepository;
import com.shopmart.repository.OrderRepository;
import com.shopmart.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private User otherUser;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("customer@shopmart.com").password("hash").role(Role.CUSTOMER).build();
        otherUser = User.builder().id(2L).email("other@shopmart.com").password("hash").role(Role.CUSTOMER).build();
        product = Product.builder()
                .id(10L)
                .name("Mechanical Keyboard")
                .price(new BigDecimal("100.00"))
                .stockQuantity(20)
                .category("Electronics")
                .active(true)
                .build();
    }

    @Test
    void placeOrder_happyPath_savesOrderAndClearsCart() {
        CartItem cartItem = CartItem.builder().id(1L).user(user).product(product).quantity(2).build();
        when(cartItemRepository.findByUser(user)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(500L);
            o.setCreatedAt(LocalDateTime.now());
            return o;
        });

        OrderResponse response = orderService.placeOrder(user);

        assertThat(response.totalAmount()).isEqualByComparingTo("200.00");
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(2);

        verify(cartItemRepository).deleteByUser(user);
        verify(productRepository).save(product);
        assertThat(product.getStockQuantity()).isEqualTo(18);
    }

    @Test
    void placeOrder_emptyCart_throwsInvalidRequestException() {
        when(cartItemRepository.findByUser(user)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.placeOrder(user))
                .isInstanceOf(InvalidRequestException.class);

        verify(orderRepository, never()).save(any());
        verify(cartItemRepository, never()).deleteByUser(any());
    }

    @Test
    void placeOrder_shouldBeTransactional() throws NoSuchMethodException {
        // BUG (intentional, OrderService#placeOrder): the method writes to orders/order_items
        // and then clears the cart as a separate step, but is not annotated @Transactional.
        // A failure partway through would leave the order and cart inconsistent. This test
        // documents the expectation that the method is transactional and is expected to fail
        // against the current implementation, which carries no such annotation.
        Method method = OrderService.class.getMethod("placeOrder", User.class);

        boolean methodAnnotated = method.isAnnotationPresent(Transactional.class);
        boolean classAnnotated = OrderService.class.isAnnotationPresent(Transactional.class);

        assertThat(methodAnnotated || classAnnotated)
                .as("OrderService.placeOrder should be @Transactional so order creation and " +
                        "cart clearing succeed or fail atomically")
                .isTrue();
    }

    @Test
    void getOrders_returnsOrdersForUser() {
        Order order = Order.builder()
                .id(1L)
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .createdAt(LocalDateTime.now())
                .build();
        when(orderRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(order));

        List<OrderResponse> responses = orderService.getOrders(user);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(1L);
    }

    @Test
    void getOrderById_ownedByUser_returnsOrder() {
        Order order = Order.builder()
                .id(1L)
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .createdAt(LocalDateTime.now())
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(user, 1L, false);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void getOrderById_belongsToAnotherUser_notAdmin_throwsAccessDeniedException() {
        Order order = Order.builder()
                .id(1L)
                .user(otherUser)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .createdAt(LocalDateTime.now())
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderById(user, 1L, false))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getOrderById_belongsToAnotherUser_asAdmin_isAllowed() {
        Order order = Order.builder()
                .id(1L)
                .user(otherUser)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .createdAt(LocalDateTime.now())
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(user, 1L, true);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void getOrderById_notFound_throwsResourceNotFoundException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(user, 999L, false))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_validTransition_updatesOrder() {
        Order order = Order.builder()
                .id(1L)
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .createdAt(LocalDateTime.now())
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.updateStatus(1L, OrderStatus.PROCESSING);

        assertThat(response.status()).isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
    void updateStatus_deliveredToPending_shouldBeRejected() {
        // BUG (intentional, OrderService#updateStatus): status transitions are not validated
        // at all, so DELIVERED -> PENDING currently succeeds silently. This test asserts the
        // CORRECT behavior (the transition should be rejected) and is expected to fail against
        // the buggy implementation.
        Order order = Order.builder()
                .id(1L)
                .user(user)
                .status(OrderStatus.DELIVERED)
                .totalAmount(new BigDecimal("50.00"))
                .createdAt(LocalDateTime.now())
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        // Stubbed so that, if the (buggy) implementation lets the transition through silently
        // instead of rejecting it, the test fails cleanly on the missing exception rather than
        // on an unrelated NullPointerException from toResponse(null).
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.PENDING))
                .as("DELIVERED -> PENDING is not a legal status transition")
                .isInstanceOf(InvalidRequestException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateStatus_orderNotFound_throwsResourceNotFoundException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(999L, OrderStatus.PROCESSING))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
