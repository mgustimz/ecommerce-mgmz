package com.example.ecommercemgmz.inventory;

import com.example.ecommercemgmz.product.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryMovementRepository inventoryMovementRepository;

    @Transactional
    public void record(Product product, Long orderId, InventoryMovementType type, int quantityChange, String reason) {
        if (quantityChange == 0) {
            return;
        }
        inventoryMovementRepository.save(new InventoryMovement(
                product.getId(),
                product.getName(),
                product.getSku(),
                orderId,
                type,
                quantityChange,
                product.getStock(),
                reason
        ));
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> findRecentMovements(Long productId) {
        List<InventoryMovement> movements = productId == null
                ? inventoryMovementRepository.findTop100ByOrderByCreatedAtDesc()
                : inventoryMovementRepository.findTop100ByProductIdOrderByCreatedAtDesc(productId);
        return movements.stream().map(InventoryMovementResponse::from).toList();
    }
}
