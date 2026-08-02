package com.shopmart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopmart.dto.OrderStatusUpdateRequest;
import com.shopmart.model.entity.CartItem;
import com.shopmart.model.entity.Order;
import com.shopmart.model.entity.Product;
import com.shopmart.model.entity.User;
import com.shopmart.model.enums.OrderStatus;
import com.shopmart.model.enums.Role;
import com.shopmart.repository.CartItemRepository;
import com.shopmart.repository.OrderRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderControllerTest {

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
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User customer;
    private User otherCustomer;
    private User admin;
    private Product product;
    private String customerAuthHeader;
    private String otherCustomerAuthHeader;
    private String adminAuthHeader;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        customer = userRepository.save(User.builder()
                .email("order-customer@shopmart.com")
                .password(passwordEncoder.encode("password1"))
                .role(Role.CUSTOMER)
                .build());
        otherCustomer = userRepository.save(User.builder()
                .email("order-other@shopmart.com")
                .password(passwordEncoder.encode("password1"))
                .role(Role.CUSTOMER)
                .build());
        admin = userRepository.save(User.builder()
                .email("order-admin@shopmart.com")
                .password(passwordEncoder.encode("password1"))
                .role(Role.ADMIN)
                .build());
        product = productRepository.save(Product.builder()
                .name("Gaming Keyboard")
                .description("RGB")
                .price(new BigDecimal("89.99"))
                .stockQuantity(50)
                .category("Electronics")
                .active(true)
                .build());

        customerAuthHeader = AuthTestUtils.bearer(jwtTokenProvider, customer);
        otherCustomerAuthHeader = AuthTestUtils.bearer(jwtTokenProvider, otherCustomer);
        adminAuthHeader = AuthTestUtils.bearer(jwtTokenProvider, admin);
    }

    @Test
    void placeOrder_withItemsInCart_returns201AndClearsCart() throws Exception {
        cartItemRepository.save(CartItem.builder().user(customer).product(product).quantity(2).build());

        mockMvc.perform(post("/api/orders").header("Authorization", customerAuthHeader))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.totalAmount", is(179.98)));

        mockMvc.perform(get("/api/cart").header("Authorization", customerAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", org.hamcrest.Matchers.hasSize(0)));
    }

    @Test
    void placeOrder_emptyCart_returns400() throws Exception {
        mockMvc.perform(post("/api/orders").header("Authorization", customerAuthHeader))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrders_returnsOrdersForCurrentUser() throws Exception {
        orderRepository.save(Order.builder()
                .user(customer)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .build());

        mockMvc.perform(get("/api/orders").header("Authorization", customerAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    void getOrderById_ownedOrder_returns200() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .user(customer)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .build());

        mockMvc.perform(get("/api/orders/" + order.getId()).header("Authorization", customerAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(order.getId().intValue())));
    }

    @Test
    void getOrderById_belongsToAnotherUser_returns403() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .user(otherCustomer)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .build());

        mockMvc.perform(get("/api/orders/" + order.getId()).header("Authorization", customerAuthHeader))
                .andExpect(status().isForbidden());
    }

    @Test
    void getOrderById_asAdmin_canViewAnyOrder() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .user(otherCustomer)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .build());

        mockMvc.perform(get("/api/orders/" + order.getId()).header("Authorization", adminAuthHeader))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatus_asAdmin_validTransition_returns200() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .user(customer)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .build());
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderStatus.PROCESSING);

        mockMvc.perform(patch("/api/orders/" + order.getId() + "/status")
                        .header("Authorization", adminAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PROCESSING")));
    }

    @Test
    void updateStatus_asNonAdmin_returns403() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .user(customer)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .build());
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderStatus.PROCESSING);

        mockMvc.perform(patch("/api/orders/" + order.getId() + "/status")
                        .header("Authorization", customerAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateStatus_deliveredToPending_shouldBeRejected() throws Exception {
        // BUG (intentional, OrderService#updateStatus / OrderController#updateStatus):
        // status transitions are not validated at all, so DELIVERED -> PENDING currently
        // succeeds with 200 OK. This test asserts the CORRECT behavior (the transition
        // should be rejected with 400) and is expected to fail against the buggy
        // implementation.
        Order order = orderRepository.save(Order.builder()
                .user(customer)
                .status(OrderStatus.DELIVERED)
                .totalAmount(new BigDecimal("50.00"))
                .build());
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderStatus.PENDING);

        mockMvc.perform(patch("/api/orders/" + order.getId() + "/status")
                        .header("Authorization", adminAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_orderNotFound_returns404() throws Exception {
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderStatus.PROCESSING);

        mockMvc.perform(patch("/api/orders/999999/status")
                        .header("Authorization", adminAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
