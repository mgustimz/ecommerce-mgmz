package com.example.ecommercemgmz.cart;

import com.example.ecommercemgmz.product.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "anonymous_carts")
public class AnonymousCart {
    @Id
    private UUID id;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnonymousCartItem> items = new ArrayList<>();

    public AnonymousCart(UUID id) {
        this.id = id;
    }

    public void addOrIncrement(Product product, int quantity) {
        AnonymousCartItem existing = items.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);
        if (existing == null) {
            AnonymousCartItem item = new AnonymousCartItem(this, product, quantity);
            items.add(item);
        } else {
            existing.setQuantity(existing.getQuantity() + quantity);
        }
    }

    public void addItem(AnonymousCartItem item) {
        item.setCart(this);
        items.add(item);
    }
}
