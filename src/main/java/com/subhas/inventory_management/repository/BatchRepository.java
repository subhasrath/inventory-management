package com.subhas.inventory_management.repository;

import com.subhas.inventory_management.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Integer> {

    /** FEFO: earliest expiry first, only batches with stock */
    @Query("""
        SELECT b FROM Batch b
        WHERE b.product.productID = :productId AND b.qtyBalance > 0
        ORDER BY b.expiryDate ASC
    """)
    List<Batch> findAvailableBatchesFefo(@Param("productId") Integer productId);

    /** Expiry alerts within N days */
    @Query("""
        SELECT b FROM Batch b
        WHERE b.qtyBalance > 0
          AND b.expiryDate <= :cutoff
        ORDER BY b.expiryDate ASC
    """)
    List<Batch> findExpiringBefore(@Param("cutoff") LocalDate cutoff);

    @Query("""
        SELECT COALESCE(SUM(b.qtyBalance), 0)
        FROM Batch b WHERE b.product.productID = :productId
    """)
    java.math.BigDecimal totalStock(@Param("productId") Integer productId);

    List<Batch> findByProductProductIDOrderByExpiryDateAsc(Integer productID);

    Optional<Batch> findByProductProductIDAndBatchNoAndExpiryDate(
            Integer productID, String batchNo, LocalDate expiryDate);
}
