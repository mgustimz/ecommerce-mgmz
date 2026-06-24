package com.example.ecommercemgmz.address;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    List<CustomerAddress> findByCustomerIdOrderByDefaultAddressDescIdDesc(Long customerId);

    Optional<CustomerAddress> findByIdAndCustomerId(Long id, Long customerId);
}
