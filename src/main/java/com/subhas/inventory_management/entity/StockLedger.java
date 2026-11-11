package com.subhas.inventory_management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_ledger", indexes = {
        @Index(name = "idx_led_batch", columnList = "batchID")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ledgerID;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batchID", nullable = false)
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "productID", nullable = false)
    private Item product;

    @Column(nullable = false, length = 20)
    private String txnType;        // PURCHASE / SALE / ADJUST / RETURN

    private Integer refID;         // purchaseID or invoiceID

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal qtyChange;  // +in / -out

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal balanceAfter;

    @Column(length = 255)
    private String remarks;

    @Column(updatable = false)
    private LocalDateTime createdOn;

    @PrePersist
    void onCreate() { this.createdOn = LocalDateTime.now(); }
}