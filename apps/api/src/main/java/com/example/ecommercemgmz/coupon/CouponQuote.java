package com.example.ecommercemgmz.coupon;

import java.math.BigDecimal;

public record CouponQuote(
        Coupon coupon,
        BigDecimal discountAmount
) {

    public String code() {
        return coupon.getCode();
    }
}
