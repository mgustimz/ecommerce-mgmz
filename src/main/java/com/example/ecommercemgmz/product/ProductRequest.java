package com.example.ecommercemgmz.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
        @NotBlank String name,
        String slug,
        @NotBlank String sku,
        String description,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @Min(0) int stock,
        @Min(0) int weightGram,
        @Min(0) int lengthCm,
        @Min(0) int widthCm,
        @Min(0) int heightCm,
        ProductShippingCategory shippingCategory,
        List<String> imageUrls,
        Long categoryId,
        @NotNull ProductStatus status
) {
}
