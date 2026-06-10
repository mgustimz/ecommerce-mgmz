package com.example.ecommercemgmz.dashboard;

import com.example.ecommercemgmz.order.CustomerOrder;
import com.example.ecommercemgmz.order.OrderStatus;
import com.example.ecommercemgmz.payment.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record RecentOrderResponse(
        Long id,
        Long customerId,
        BigDecimal total,
        OrderStatus status,
        PaymentStatus paymentStatus,
        Instant createdAt
) {
    public static RecentOrderResponse from(CustomerOrder order) {
        return new RecentOrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getTotal(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getCreatedAt()
        );
    }
}
