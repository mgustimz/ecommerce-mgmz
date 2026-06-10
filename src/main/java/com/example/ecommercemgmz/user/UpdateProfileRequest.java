package com.example.ecommercemgmz.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank String name,
        String phone
) {
}
