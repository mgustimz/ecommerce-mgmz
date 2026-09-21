package com.example.ecommercemgmz.auth;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank String token,
        String password
) {
}
