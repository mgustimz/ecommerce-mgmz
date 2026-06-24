package com.example.ecommercemgmz.dashboard;

import com.example.ecommercemgmz.product.Product;
import com.example.ecommercemgmz.product.ProductStatus;

public record LowStockProductResponse(
        Long id,
        String name,
        String sku,
        int stock,
        ProductStatus status
) {
    public static LowStockProductResponse from(Product product) {
        return new LowStockProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getStock(),
                product.getStatus()
        );
    }
}
