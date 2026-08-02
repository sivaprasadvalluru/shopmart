package com.shopmart.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        List<CartItemResponse> items,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal grandTotal
) {
}
