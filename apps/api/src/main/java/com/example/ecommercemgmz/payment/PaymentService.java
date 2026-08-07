package com.example.ecommercemgmz.payment;

import com.example.ecommercemgmz.common.ApiException;
import com.example.ecommercemgmz.order.CustomerOrder;
import com.example.ecommercemgmz.order.CustomerOrderRepository;
import com.example.ecommercemgmz.order.OrderResponse;
import com.example.ecommercemgmz.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final CustomerOrderRepository orderRepository;

    @Transactional(readOnly = true)
    public PaymentResponse getCustomerPayment(Long customerId, Long orderId) {
        CustomerOrder order = findOrder(orderId);
        if (!order.getCustomerId().equals(customerId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Payment not found");
        }
        return PaymentResponse.from(order);
    }

    @Transactional
    public OrderResponse simulatePaid(Long orderId) {
        CustomerOrder order = findOrder(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cancelled order cannot be paid");
        }
        if (order.getPaymentStatus() == PaymentStatus.FAILED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Expired or failed payment cannot be paid");
        }
        if (Instant.now().isAfter(order.getPaymentExpiresAt())) {
            order.markPaymentFailed();
            throw new ApiException(HttpStatus.BAD_REQUEST, "Payment has expired");
        }
        order.markPaid();
        return OrderResponse.from(order);
    }

    @Transactional
    public PaymentResponse expirePayment(Long orderId) {
        CustomerOrder order = findOrder(orderId);
        if (order.getPaymentStatus() == PaymentStatus.PENDING) {
            order.markPaymentFailed();
        }
        return PaymentResponse.from(order);
    }

    private CustomerOrder findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
    }
}
