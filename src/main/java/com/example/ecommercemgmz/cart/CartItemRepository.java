package com.example.ecommercemgmz.cart;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCustomerIdOrderByIdAsc(Long customerId);

    Optional<CartItem> findByCustomerIdAndProductId(Long customerId, Long productId);

    Optional<CartItem> findByIdAndCustomerId(Long id, Long customerId);

    void deleteByCustomerId(Long customerId);
}
