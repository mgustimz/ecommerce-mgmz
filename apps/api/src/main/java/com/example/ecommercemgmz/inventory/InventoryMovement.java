package com.example.ecommercemgmz.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "inventory_movements")
public class InventoryMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String productSku;

    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryMovementType type;

    @Column(nullable = false)
    private int quantityChange;

    @Column(nullable = false)
    private int stockAfter;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public InventoryMovement(Long productId, String productName, String productSku, Long orderId, InventoryMovementType type, int quantityChange, int stockAfter, String reason) {
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.orderId = orderId;
        this.type = type;
        this.quantityChange = quantityChange;
        this.stockAfter = stockAfter;
        this.reason = reason;
    }
}
