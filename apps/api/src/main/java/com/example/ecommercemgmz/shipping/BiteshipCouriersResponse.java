package com.example.ecommercemgmz.shipping;

import java.util.List;

public record BiteshipCouriersResponse(
        boolean success,
        String object,
        List<BiteshipCourierResponse> couriers
) {
}
