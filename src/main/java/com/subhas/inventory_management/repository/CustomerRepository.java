package com.subhas.inventory_management.repository;

import com.subhas.inventory_management.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    // ---- existing ----
    List<Customer> findByFullNameContainingIgnoreCase(String name);
    List<Customer> findByStatus(String status);

    // ---- walk-in reuse ----
    Optional<Customer> findFirstByFullNameIgnoreCaseAndCustomerType(
            String fullName, String customerType);

    // ---- walk-in autocomplete for billing screen ----
    List<Customer> findByFullNameContainingIgnoreCaseAndCustomerTypeAndStatus(
            String q, String customerType, String status);

    // ---- reserved "Cash Customer" lookup ----
    Optional<Customer> findFirstByFullNameIgnoreCaseAndCustomerTypeOrderByCustomerIDAsc(
            String fullName, String customerType);
}