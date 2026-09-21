package com.example.ecommercemgmz.coupon;

import com.example.ecommercemgmz.auth.AuthenticatedUser;
import com.example.ecommercemgmz.cart.CartItem;
import com.example.ecommercemgmz.cart.CartService;
import com.example.ecommercemgmz.common.ApiException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;
    private final CartService cartService;

    @PostMapping("/validate")
    CouponPreview validate(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody ValidateCouponRequest request) {
        List<CartItem> cartItems = cartService.findItemsForCheckout(user.id());
        if (cartItems.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }
        BigDecimal subtotal = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        CouponQuote quote = couponService.quote(request.code(), user.id(), subtotal);
        return new CouponPreview(quote.code(), quote.discountAmount());
    }
}
