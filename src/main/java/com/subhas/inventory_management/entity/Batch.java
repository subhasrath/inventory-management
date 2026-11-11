package com.subhas.inventory_management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_batch",
                columnNames = {"productID", "batchNo", "expiryDate"}),
        indexes = {
                @Index(name = "idx_batch_expiry", columnList = "expiryDate"),
                @Index(name = "idx_batch_product", columnList = "productID")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer batchID;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "productID", nullable = false)
    private Item product;

    @Column(nullable = false, length = 60)
    private String batchNo;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal mrp = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal purchaseRate = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal saleRate = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal qtyIn = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal qtyBalance = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendorID")
    private Vendor vendor;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    @Column(updatable = false)
    private LocalDateTime createdOn;

    @PrePersist
    void onCreate() { this.createdOn = LocalDateTime.now(); }
}