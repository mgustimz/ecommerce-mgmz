package com.example.ecommercemgmz.payment;

import com.example.ecommercemgmz.common.ApiException;
import com.example.ecommercemgmz.inventory.InventoryMovementType;
import com.example.ecommercemgmz.inventory.InventoryService;
import com.example.ecommercemgmz.order.CustomerOrder;
import com.example.ecommercemgmz.order.CustomerOrderRepository;
import com.example.ecommercemgmz.order.OrderItem;
import com.example.ecommercemgmz.order.OrderResponse;
import com.example.ecommercemgmz.order.OrderStatus;
import com.example.ecommercemgmz.product.Product;
import com.example.ecommercemgmz.product.ProductRepository;
import com.example.ecommercemgmz.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final CustomerOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;

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
            releaseReservation(order, "Payment expired");
            throw new ApiException(HttpStatus.BAD_REQUEST, "Payment has expired");
        }
        order.markPaid();
        return OrderResponse.from(order);
    }

    @Transactional
    public PaymentResponse expirePayment(Long orderId) {
        CustomerOrder order = findOrder(orderId);
        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only pending payments can be expired");
        }
        releaseReservation(order, "Payment expired");
        return PaymentResponse.from(order);
    }

    /**
     * Releases held stock for every pending-payment order whose payment window elapsed.
     * Scheduled every 60s by PaymentExpiryScheduler.
     * ponytail: in-process scheduler; add a distributed lock (ShedLock) when this runs on more than one instance.
     */
    @Transactional
    public int expireStaleOrders() {
        List<CustomerOrder> staleOrders = orderRepository
                .findByStatusAndPaymentStatusAndPaymentExpiresAtBefore(OrderStatus.PENDING_PAYMENT, PaymentStatus.PENDING, Instant.now());
        for (CustomerOrder order : staleOrders) {
            releaseReservation(order, "Payment expired");
        }
        if (!staleOrders.isEmpty()) {
            log.info("Released stock for {} expired order(s)", staleOrders.size());
        }
        return staleOrders.size();
    }

    /**
     * Cancels the order, restores its reserved stock, records inventory movements, and marks payment FAILED.
     */
    private void releaseReservation(CustomerOrder order, String cancelReason) {
        for (OrderItem item : order.getItems()) {
            Product product = productService.findEntity(item.getProductId());
            int restored = productRepository.restoreStock(product.getId(), item.getQuantity());
            if (restored == 0) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to release stock for " + product.getName());
            }
            product.setStock(product.getStock() + item.getQuantity());
            inventoryService.record(product, order.getId(), InventoryMovementType.ORDER_CANCELLED, item.getQuantity(), "Stock released after payment expiry");
        }
        order.markPaymentFailed();
        order.cancel(cancelReason);
    }

    private CustomerOrder findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
    }
}
