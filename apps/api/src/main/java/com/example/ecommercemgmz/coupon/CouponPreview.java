package com.example.ecommercemgmz.coupon;

import java.math.BigDecimal;

public record CouponPreview(
        String code,
        BigDecimal discountAmount
) {
}
