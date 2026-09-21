package com.example.ecommercemgmz.coupon;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {
    private final CouponService couponService;

    @GetMapping
    List<CouponResponse> list() {
        return couponService.findAll();
    }

    @PostMapping
    ResponseEntity<CouponResponse> create(@Valid @RequestBody CouponInput input) {
        CouponResponse created = couponService.create(input);
        return ResponseEntity.created(URI.create("/api/admin/coupons/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    CouponResponse update(@PathVariable Long id, @Valid @RequestBody CouponInput input) {
        return couponService.update(id, input);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        couponService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
