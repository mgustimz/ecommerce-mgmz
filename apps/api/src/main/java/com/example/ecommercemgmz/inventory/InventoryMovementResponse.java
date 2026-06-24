package com.example.ecommercemgmz.inventory;

import java.time.Instant;

public record InventoryMovementResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        Long orderId,
        InventoryMovementType type,
        int quantityChange,
        int stockAfter,
        String reason,
        Instant createdAt
) {
    public static InventoryMovementResponse from(InventoryMovement movement) {
        return new InventoryMovementResponse(
                movement.getId(),
                movement.getProductId(),
                movement.getProductName(),
                movement.getProductSku(),
                movement.getOrderId(),
                movement.getType(),
                movement.getQuantityChange(),
                movement.getStockAfter(),
                movement.getReason(),
                movement.getCreatedAt()
        );
    }
}
