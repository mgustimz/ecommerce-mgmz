package com.example.ecommercemgmz.shipping;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record ShippingRateRequest(
        @NotNull Long addressId,
        List<String> courierCodes,
        String type,
        BigDecimal courierInsurance,
        BigDecimal destinationCashOnDelivery,
        String destinationCashOnDeliveryType
) {
}
