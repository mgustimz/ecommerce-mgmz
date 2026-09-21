package com.example.ecommercemgmz.coupon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "coupons")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponDiscountType discountType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal discountValue;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal minSubtotal;

    @Column(nullable = false)
    private Instant validFrom;

    private Instant validUntil;

    private Integer maxRedemptions;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Coupon(String code, CouponDiscountType discountType, BigDecimal discountValue, BigDecimal minSubtotal, Instant validFrom, Instant validUntil, Integer maxRedemptions, boolean active) {
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minSubtotal = minSubtotal;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.maxRedemptions = maxRedemptions;
        this.active = active;
    }

    public boolean isWithinValidityWindow(Instant now) {
        return !now.isBefore(validFrom) && (validUntil == null || now.isBefore(validUntil));
    }
}
