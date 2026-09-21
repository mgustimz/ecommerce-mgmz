package com.example.ecommercemgmz.coupon;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, Long> {
    long countByCouponId(Long couponId);

    boolean existsByCouponIdAndCustomerId(Long couponId, Long customerId);

    List<CouponRedemption> findByOrderId(Long orderId);
}
