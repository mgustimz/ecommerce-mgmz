package com.example.ecommercemgmz.coupon;

import com.example.ecommercemgmz.common.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository redemptionRepository;

    @Transactional(readOnly = true)
    public List<CouponResponse> findAll() {
        return couponRepository.findAll().stream()
                .map(coupon -> CouponResponse.from(coupon, redemptionRepository.countByCouponId(coupon.getId())))
                .toList();
    }

    @Transactional
    public CouponResponse create(CouponInput input) {
        String code = normalize(input.code());
        requireUniqueCode(code, null);
        Coupon coupon = new Coupon(
                code,
                input.discountType(),
                input.discountValue(),
                input.minSubtotal() == null ? BigDecimal.ZERO : input.minSubtotal(),
                input.validFrom(),
                input.validUntil(),
                input.maxRedemptions(),
                input.active()
        );
        couponRepository.save(coupon);
        return CouponResponse.from(coupon, 0);
    }

    @Transactional
    public CouponResponse update(Long id, CouponInput input) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Coupon not found"));
        String code = normalize(input.code());
        requireUniqueCode(code, id);
        coupon.setCode(code);
        coupon.setDiscountType(input.discountType());
        coupon.setDiscountValue(input.discountValue());
        coupon.setMinSubtotal(input.minSubtotal() == null ? BigDecimal.ZERO : input.minSubtotal());
        coupon.setValidFrom(input.validFrom());
        coupon.setValidUntil(input.validUntil());
        coupon.setMaxRedemptions(input.maxRedemptions());
        coupon.setActive(input.active());
        return CouponResponse.from(coupon, redemptionRepository.countByCouponId(coupon.getId()));
    }

    @Transactional
    public void delete(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Coupon not found");
        }
        couponRepository.deleteById(id);
    }

    /**
     * Validates a coupon code against the customer's cart subtotal and returns the discount amount.
     */
    @Transactional(readOnly = true)
    public CouponQuote quote(String rawCode, Long customerId, BigDecimal subtotal) {
        Coupon coupon = usableCoupon(rawCode, customerId, subtotal);
        return new CouponQuote(coupon, discountAmount(coupon, subtotal));
    }

    private Coupon usableCoupon(String rawCode, Long customerId, BigDecimal subtotal) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(normalize(rawCode))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Coupon code is not valid"));
        Instant now = Instant.now();
        if (!coupon.isActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Coupon is not active");
        }
        if (!coupon.isWithinValidityWindow(now)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Coupon is not in its validity period");
        }
        if (subtotal.compareTo(coupon.getMinSubtotal()) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order does not meet the minimum spend for this coupon");
        }
        if (coupon.getMaxRedemptions() != null && redemptionRepository.countByCouponId(coupon.getId()) >= coupon.getMaxRedemptions()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Coupon redemption limit has been reached");
        }
        if (redemptionRepository.existsByCouponIdAndCustomerId(coupon.getId(), customerId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You have already used this coupon");
        }
        return coupon;
    }

    private void requireUniqueCode(String code, Long ignoreId) {
        couponRepository.findByCodeIgnoreCase(code)
                .filter(existing -> ignoreId == null || !existing.getId().equals(ignoreId))
                .ifPresent(other -> { throw new ApiException(HttpStatus.CONFLICT, "Coupon code already exists"); });
    }

    static BigDecimal discountAmount(Coupon coupon, BigDecimal subtotal) {
        if (coupon.getDiscountType() == CouponDiscountType.PERCENT) {
            BigDecimal raw = subtotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_DOWN);
            return raw.min(subtotal).max(BigDecimal.ZERO);
        }
        return coupon.getDiscountValue().min(subtotal).max(BigDecimal.ZERO);
    }

    private String normalize(String rawCode) {
        return (rawCode == null ? "" : rawCode.trim().toUpperCase(Locale.ROOT));
    }
}
