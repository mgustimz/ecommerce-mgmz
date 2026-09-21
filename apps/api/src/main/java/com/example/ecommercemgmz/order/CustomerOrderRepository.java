package com.example.ecommercemgmz.order;

import com.example.ecommercemgmz.payment.PaymentStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<CustomerOrder> findTop10ByOrderByCreatedAtDesc();

    List<CustomerOrder> findByStatusAndPaymentStatusAndPaymentExpiresAtBefore(OrderStatus status, PaymentStatus paymentStatus, Instant cutoff);
}
