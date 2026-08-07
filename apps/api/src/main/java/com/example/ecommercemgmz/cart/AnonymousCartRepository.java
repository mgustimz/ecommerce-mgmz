package com.example.ecommercemgmz.cart;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnonymousCartRepository extends JpaRepository<AnonymousCart, UUID> {
}
