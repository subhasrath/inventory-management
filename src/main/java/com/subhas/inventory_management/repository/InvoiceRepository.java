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

    @Query("SELECT COALESCE(SUM(i.grandTotal),0) FROM Invoice i WHERE i.invoiceDate = :d")
    java.math.BigDecimal totalForDate(@Param("d") LocalDate date);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.invoiceDate = :d")
    long countForDate(@Param("d") LocalDate date);
}
