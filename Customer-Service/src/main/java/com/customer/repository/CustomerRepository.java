package com.customer.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.customer.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByMobile(String mobile);
    Optional<Customer> findByEmailIgnoreCase(String email);
    Optional<Customer> findByMobile(String mobile);
}
