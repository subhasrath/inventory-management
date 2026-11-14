package com.subhas.inventory_management.repository;

import com.subhas.inventory_management.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Integer> { }
