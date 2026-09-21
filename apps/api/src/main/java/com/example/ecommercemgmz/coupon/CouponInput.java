package com.example.ecommercemgmz.coupon;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public record CouponInput(
        @NotBlank String code,
        @NotNull CouponDiscountType discountType,
        @NotNull @DecimalMin("0.01") BigDecimal discountValue,
        BigDecimal minSubtotal,
        Instant validFrom,
        Instant validUntil,
        Integer maxRedemptions,
        boolean active
) {
}
