package com.example.ecommercemgmz.coupon;

import com.example.ecommercemgmz.order.CustomerOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coupon_redemptions")
public class CouponRedemption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal discountAmount;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public CouponRedemption(Coupon coupon, CustomerOrder order, Long customerId, BigDecimal discountAmount) {
        this.coupon = coupon;
        this.order = order;
        this.customerId = customerId;
        this.discountAmount = discountAmount;
    }
}
