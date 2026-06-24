package com.example.ecommercemgmz.inventory;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/inventory-movements")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    List<InventoryMovementResponse> findRecentMovements(@RequestParam(required = false) Long productId) {
        return inventoryService.findRecentMovements(productId);
    }
}
