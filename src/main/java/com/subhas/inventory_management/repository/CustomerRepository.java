package com.subhas.inventory_management.repository;

import com.subhas.inventory_management.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    List<Customer> findByFullNameContainingIgnoreCase(String name);
    List<Customer> findByStatus(String status);
}
