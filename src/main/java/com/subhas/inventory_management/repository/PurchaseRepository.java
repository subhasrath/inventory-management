package com.subhas.inventory_management.repository;

import com.subhas.inventory_management.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
    @Query("""
    SELECT DISTINCT p FROM Purchase p
    LEFT JOIN FETCH p.vendor
    LEFT JOIN FETCH p.items pi
    LEFT JOIN FETCH pi.product
    WHERE (:q IS NULL
           OR LOWER(p.purchaseNo)   LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(p.vendorBillNo) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(p.vendor.fullName) LIKE LOWER(CONCAT('%', :q, '%')))
      AND (:vendorID IS NULL OR p.vendor.vendorID = :vendorID)
      AND (:from IS NULL OR p.purchaseDate >= :from)
      AND (:to   IS NULL OR p.purchaseDate <= :to)
    ORDER BY p.purchaseDate DESC, p.purchaseID DESC
""")
    List<Purchase> search(@Param("q") String q,
                          @Param("vendorID") Integer vendorID,
                          @Param("from") LocalDate from,
                          @Param("to") LocalDate to);

    @Query("""
    SELECT p.vendor.vendorID,
           p.vendor.fullName,
           p.vendor.gstin,
           COUNT(DISTINCT p.purchaseID),
           SUM(p.subTotal),
           SUM(p.cgst),
           SUM(p.sgst),
           SUM(p.igst),
           SUM(p.grandTotal)
    FROM Purchase p
    WHERE (:from IS NULL OR p.purchaseDate >= :from)
      AND (:to   IS NULL OR p.purchaseDate <= :to)
    GROUP BY p.vendor.vendorID, p.vendor.fullName, p.vendor.gstin
    ORDER BY SUM(p.grandTotal) DESC
""")
    List<Object[]> vendorSummary(@Param("from") LocalDate from,
                                 @Param("to")   LocalDate to);
}
