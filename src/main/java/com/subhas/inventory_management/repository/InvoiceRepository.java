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
}
