package com.example.ecommercemgmz.payment;

import com.example.ecommercemgmz.order.CustomerOrder;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long orderId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        String reference,
        Instant expiresAt
) {
    public static PaymentResponse from(CustomerOrder order) {
        return new PaymentResponse(
                order.getId(),
                order.getTotal(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getPaymentReference(),
                order.getPaymentExpiresAt()
        );
    }
}
