package com.example.ecommercemgmz.order;

import jakarta.validation.constraints.NotBlank;

public record CancelOrderRequest(
        @NotBlank String reason
) {
}
