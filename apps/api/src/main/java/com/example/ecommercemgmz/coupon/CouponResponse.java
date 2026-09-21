package com.example.ecommercemgmz.coupon;

import java.math.BigDecimal;

public record CouponResponse(
        Long id,
        String code,
        CouponDiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minSubtotal,
        java.time.Instant validFrom,
        java.time.Instant validUntil,
        Integer maxRedemptions,
        boolean active,
        long redemptions
) {
    public static CouponResponse from(Coupon coupon, long redemptions) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getMinSubtotal(),
                coupon.getValidFrom(),
                coupon.getValidUntil(),
                coupon.getMaxRedemptions(),
                coupon.isActive(),
                redemptions
        );
    }
}
