package com.example.ecommercemgmz.order;

import com.example.ecommercemgmz.payment.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record CheckoutRequest(
        @NotNull Long addressId,
        @NotBlank String shippingServiceCode,
        @NotNull PaymentMethod paymentMethod,
        String notes
) {
}
