package com.subhas.inventory_management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "item", indexes = {
        @Index(name = "idx_item_name", columnList = "itemName"),
        @Index(name = "idx_item_barcode", columnList = "barcode")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productID;

    @Column(nullable = false, unique = true, length = 60)
    private String itemNumber;

    @Column(unique = true, length = 60)
    private String barcode;

    @Column(nullable = false, length = 255)
    private String itemName;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20)
    private String hsnCode;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal gstRate = new BigDecimal("18.00");

    @Column(nullable = false, length = 10)
    private String unit = "PCS";

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer reorderLevel = 10;

    @Column(length = 300)
    private String imageURL;

    @Column(nullable = false, length = 20)
    private String status = "Active";

    @Column(updatable = false)
    private LocalDateTime createdOn;

    @PrePersist
    void onCreate() { this.createdOn = LocalDateTime.now(); }
}
