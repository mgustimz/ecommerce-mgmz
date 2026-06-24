package com.example.ecommercemgmz.product;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String slug,
        String sku,
        String description,
        BigDecimal price,
        int stock,
        int weightGram,
        int lengthCm,
        int widthCm,
        int heightCm,
        ProductShippingCategory shippingCategory,
        ProductStatus status,
        List<String> imageUrls,
        Long categoryId,
        String categoryName
) {
    public static ProductResponse from(Product product) {
        Long categoryId = product.getCategory() == null ? null : product.getCategory().getId();
        String categoryName = product.getCategory() == null ? null : product.getCategory().getName();
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getSku(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getWeightGram(),
                product.getLengthCm(),
                product.getWidthCm(),
                product.getHeightCm(),
                product.getShippingCategory(),
                product.getStatus(),
                product.getImages().stream()
                        .sorted(Comparator.comparingInt(ProductImage::getSortOrder))
                        .map(ProductImage::getImageUrl)
                        .toList(),
                categoryId,
                categoryName
        );
    }
}
