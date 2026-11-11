package com.subhas.inventory_management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "purchase_item")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer purchaseItemID;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchaseID", nullable = false)
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "productID", nullable = false)
    private Item product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batchID", nullable = false)
    private Batch batch;

    @Column(nullable = false, length = 60)
    private String batchNo;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal mrp = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal purchaseRate;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal gstRate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal taxableValue;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;
}