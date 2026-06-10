package com.example.ecommercemgmz.order;

import com.example.ecommercemgmz.payment.PaymentMethod;
import com.example.ecommercemgmz.payment.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        Long customerId,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal total,
        String shippingAddress,
        String notes,
        String cancellationReason,
        Instant cancelledAt,
        OrderStatus status,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        String paymentReference,
        Instant paymentExpiresAt,
        String shippingServiceCode,
        String shippingServiceName,
        Instant createdAt,
        List<OrderItemResponse> items
) {
    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getSubtotal(),
                order.getShippingFee(),
                order.getTotal(),
                order.getShippingAddress(),
                order.getNotes(),
                order.getCancellationReason(),
                order.getCancelledAt(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getPaymentMethod(),
                order.getPaymentReference(),
                order.getPaymentExpiresAt(),
                order.getShippingServiceCode(),
                order.getShippingServiceName(),
                order.getCreatedAt(),
                order.getItems().stream().map(OrderItemResponse::from).toList()
        );
    }
}
