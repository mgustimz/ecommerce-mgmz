package com.example.ecommercemgmz.address;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record AddressRequest(
        @NotBlank String label,
        @NotBlank String recipientName,
        @NotBlank String phone,
        @NotBlank String street,
        @NotBlank String city,
        @NotBlank String province,
        @NotBlank String postalCode,
        String areaId,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean defaultAddress
) {
}
