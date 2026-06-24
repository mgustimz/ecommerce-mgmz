package com.example.ecommercemgmz.shipping;

import java.math.BigDecimal;

public record ExternalShippingRateRequest(
        String destinationCity,
        String destinationProvince,
        String destinationPostalCode,
        int totalWeightGram,
        BigDecimal subtotal
) {
}
