package com.shopmart.service;

import com.shopmart.dto.CartItemRequest;
import com.shopmart.dto.CartItemResponse;
import com.shopmart.dto.CartResponse;
import com.shopmart.exception.InvalidRequestException;
import com.shopmart.exception.ResourceNotFoundException;
import com.shopmart.model.entity.CartItem;
import com.shopmart.model.entity.Product;
import com.shopmart.model.entity.User;
import com.shopmart.model.enums.Role;
import com.shopmart.repository.CartItemRepository;
import com.shopmart.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("customer@shopmart.com").password("hash").role(Role.CUSTOMER).build();
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
    void addItem_newProduct_createsCartItemWithRequestedQuantity() {
        CartItemRequest request = new CartItemRequest(10L, 3);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserAndProductId(user, 10L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> {
            CartItem toSave = inv.getArgument(0);
            toSave.setId(100L);
            return toSave;
        });

        CartItemResponse response = cartService.addItem(user, request);

        assertThat(response.quantity()).isEqualTo(3);
        assertThat(response.productId()).isEqualTo(10L);
        assertThat(response.lineTotal()).isEqualByComparingTo("300.00");

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(3);
        assertThat(captor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    void addItem_existingProduct_incrementsQuantity() {
        CartItemRequest request = new CartItemRequest(10L, 2);
        CartItem existing = CartItem.builder().id(50L).user(user).product(product).quantity(1).build();
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserAndProductId(user, 10L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CartItemResponse response = cartService.addItem(user, request);

        assertThat(response.quantity()).isEqualTo(3);
        assertThat(response.lineTotal()).isEqualByComparingTo("300.00");
    }

    @Test
    void addItem_quantityExceedsStock_throwsInvalidRequestException() {
        CartItemRequest request = new CartItemRequest(10L, 999);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addItem(user, request))
                .isInstanceOf(InvalidRequestException.class);

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addItem_productNotFound_throwsResourceNotFoundException() {
        CartItemRequest request = new CartItemRequest(999L, 1);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(user, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCart_emptyCart_returnsZeroedTotals() {
        when(cartItemRepository.findByUser(user)).thenReturn(List.of());

        CartResponse response = cartService.getCart(user);

        assertThat(response.items()).isEmpty();
        assertThat(response.subtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.discount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.grandTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getCart_appliesLoyaltyDiscountExactlyOnce() {
        // BUG (intentional, CartService#getCart): the 10% loyalty discount is currently
        // subtracted from the subtotal twice, so grandTotal ends up at subtotal * 0.80
        // instead of the correct subtotal * 0.90. This test asserts the CORRECT behavior
        // and is expected to fail against the buggy implementation.
        CartItem item = CartItem.builder().id(1L).user(user).product(product).quantity(2).build();
        when(cartItemRepository.findByUser(user)).thenReturn(List.of(item));

        CartResponse response = cartService.getCart(user);

        BigDecimal expectedSubtotal = new BigDecimal("200.00");
        BigDecimal expectedDiscount = new BigDecimal("20.000");
        BigDecimal expectedGrandTotal = expectedSubtotal.multiply(new BigDecimal("0.90"));

        assertThat(response.subtotal()).isEqualByComparingTo(expectedSubtotal);
        assertThat(response.discount()).isEqualByComparingTo(expectedDiscount);
        assertThat(response.grandTotal())
                .as("grandTotal should equal subtotal minus a single 10%% discount")
                .isEqualByComparingTo(expectedGrandTotal);
    }

    @Test
    void removeItem_ownedItem_deletesIt() {
        CartItem item = CartItem.builder().id(1L).user(user).product(product).quantity(1).build();
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));

        cartService.removeItem(user, 1L);

        verify(cartItemRepository).delete(item);
    }

    @Test
    void removeItem_notFound_throwsResourceNotFoundException() {
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeItem(user, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeItem_belongsToAnotherUser_throwsAccessDeniedException() {
        User otherUser = User.builder().id(2L).email("other@shopmart.com").password("hash").role(Role.CUSTOMER).build();
        CartItem item = CartItem.builder().id(1L).user(otherUser).product(product).quantity(1).build();
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> cartService.removeItem(user, 1L))
                .isInstanceOf(AccessDeniedException.class);

        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }
}
