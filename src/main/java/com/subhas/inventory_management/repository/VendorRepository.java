package com.subhas.inventory_management.repository;

import com.subhas.inventory_management.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorRepository extends JpaRepository<Vendor, Integer> { }
