package com.subhas.inventory_management.services;

import com.subhas.inventory_management.entity.*;
import com.subhas.inventory_management.repository.BatchRepository;
import com.subhas.inventory_management.repository.StockLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * FEFO (First-Expiry-First-Out) allocation.
 * Given a product and quantity needed, splits the deduction
 * across earliest-expiring batches.
 */
@Service
@RequiredArgsConstructor
public class BatchAllocationService {

    private final BatchRepository batchRepository;
    private final StockLedgerRepository ledgerRepository;

    public record Allocation(Batch batch, BigDecimal qty) {}

    /** Returns batch allocations; throws if insufficient stock. */
    @Transactional(readOnly = true)
    public List<Allocation> allocate(Integer productId, BigDecimal qtyNeeded) {
        List<Batch> batches = batchRepository.findAvailableBatchesFefo(productId);
        List<Allocation> result = new ArrayList<>();
        BigDecimal remaining = qtyNeeded;

        for (Batch b : batches) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal take = b.getQtyBalance().min(remaining);
            result.add(new Allocation(b, take));
            remaining = remaining.subtract(take);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException(
                    "Insufficient stock for product " + productId +
                            ". Short by " + remaining);
        }
        return result;
    }

    /** Deducts the allocated quantity and writes the ledger. */
    @Transactional
    public void deduct(Allocation alloc, Integer invoiceId, Item product) {
        Batch b = alloc.batch();
        b.setQtyBalance(b.getQtyBalance().subtract(alloc.qty()));
        batchRepository.save(b);

        ledgerRepository.save(StockLedger.builder()
                .batch(b)
                .product(product)
                .txnType("SALE")
                .refID(invoiceId)
                .qtyChange(alloc.qty().negate())
                .balanceAfter(b.getQtyBalance())
                .build());
    }
}