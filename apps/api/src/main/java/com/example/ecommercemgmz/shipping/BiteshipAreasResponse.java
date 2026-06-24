package com.example.ecommercemgmz.shipping;

import java.util.List;

public record BiteshipAreasResponse(
        boolean success,
        List<BiteshipAreaResponse> areas
) {
}
