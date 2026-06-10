package com.example.ecommercemgmz.order;

import com.example.ecommercemgmz.payment.PaymentStatus;
import com.example.ecommercemgmz.payment.PaymentMethod;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal shippingFee;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @Column(nullable = false)
    private String shippingAddress;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(columnDefinition = "text")
    private String cancellationReason;

    private Instant cancelledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private String paymentReference;

    @Column(nullable = false)
    private Instant paymentExpiresAt;

    @Column(nullable = false)
    private String shippingServiceCode;

    @Column(nullable = false)
    private String shippingServiceName;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    protected CustomerOrder() {
    }

    public CustomerOrder(Long customerId, BigDecimal subtotal, BigDecimal shippingFee, BigDecimal total, String shippingAddress, String notes, PaymentMethod paymentMethod, String paymentReference, Instant paymentExpiresAt, String shippingServiceCode, String shippingServiceName) {
        this.customerId = customerId;
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.total = total;
        this.shippingAddress = shippingAddress;
        this.notes = notes;
        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;
        this.paymentExpiresAt = paymentExpiresAt;
        this.shippingServiceCode = shippingServiceCode;
        this.shippingServiceName = shippingServiceName;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public String getNotes() {
        return notes;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void cancel(String reason) {
        this.status = OrderStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledAt = Instant.now();
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public Instant getPaymentExpiresAt() {
        return paymentExpiresAt;
    }

    public String getShippingServiceCode() {
        return shippingServiceCode;
    }

    public String getShippingServiceName() {
        return shippingServiceName;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void markPaid() {
        this.paymentStatus = PaymentStatus.PAID;
        this.status = OrderStatus.PAID;
    }

    public void markPaymentFailed() {
        this.paymentStatus = PaymentStatus.FAILED;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
