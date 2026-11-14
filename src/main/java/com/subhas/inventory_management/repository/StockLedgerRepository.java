package com.subhas.inventory_management.repository;

import com.subhas.inventory_management.entity.StockLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockLedgerRepository extends JpaRepository<StockLedger, Integer> {
    List<StockLedger> findByBatchBatchIDOrderByCreatedOnDesc(Integer batchID);
}