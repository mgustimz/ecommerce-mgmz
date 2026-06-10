package com.example.ecommercemgmz.shipping;

import java.math.BigDecimal;

public record ShippingRateResponse(
        String serviceCode,
        String courierName,
        String serviceName,
        BigDecimal fee,
        String estimatedDelivery
) {
}
