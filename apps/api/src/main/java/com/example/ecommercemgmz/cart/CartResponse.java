package com.example.ecommercemgmz.cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long customerId,
        List<CartItemResponse> items,
        BigDecimal subtotal
) {
    public static CartResponse from(Long customerId, List<CartItem> items) {
        List<CartItemResponse> itemResponses = items.stream().map(CartItemResponse::from).toList();
        BigDecimal subtotal = itemResponses.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(customerId, itemResponses, subtotal);
    }
}
