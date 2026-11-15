package com.subhas.inventory_management.repository;

import com.subhas.inventory_management.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    Optional<Invoice> findByInvoiceNo(String invoiceNo);

    List<Invoice> findByInvoiceDateBetween(LocalDate from, LocalDate to);

    List<Invoice> findByCustomerCustomerIDOrderByInvoiceDateDesc(Integer customerID);

    @Query("SELECT COALESCE(SUM(i.grandTotal),0) FROM Invoice i WHERE i.invoiceDate = :d")
    java.math.BigDecimal totalForDate(@Param("d") LocalDate date);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.invoiceDate = :d")
    long countForDate(@Param("d") LocalDate date);

    @Query("""
    SELECT DISTINCT i FROM Invoice i
    LEFT JOIN FETCH i.items it
    LEFT JOIN FETCH it.product
    LEFT JOIN FETCH it.batch
    WHERE i.invoiceID = :id
""")
    Optional<Invoice> findByIdWithItemsAndProducts(@Param("id") Integer id);

    @Query("""
    SELECT DISTINCT i FROM Invoice i
    LEFT JOIN FETCH i.items it
    LEFT JOIN FETCH it.product
    LEFT JOIN FETCH it.batch
    WHERE i.invoiceNo = :no
""")
    Optional<Invoice> findByInvoiceNoWithItems(@Param("no") String invoiceNo);

    // InvoiceRepository.java
    @Query("""
    SELECT i FROM Invoice i
    WHERE (:q IS NULL OR LOWER(i.invoiceNo) LIKE LOWER(CONCAT('%', :q, '%'))
                     OR LOWER(i.customerName) LIKE LOWER(CONCAT('%', :q, '%'))
                     OR LOWER(i.customerGstin) LIKE LOWER(CONCAT('%', :q, '%')))
      AND (:from IS NULL OR i.invoiceDate >= :from)
      AND (:to   IS NULL OR i.invoiceDate <= :to)
    ORDER BY i.invoiceDate DESC, i.invoiceID DESC
""")
    List<Invoice> search(@Param("q") String q,
                         @Param("from") LocalDate from,
                         @Param("to") LocalDate to);
}
