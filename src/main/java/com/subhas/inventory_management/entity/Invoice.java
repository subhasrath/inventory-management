package com.subhas.inventory_management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoice", indexes = {
        @Index(name = "idx_inv_date", columnList = "invoiceDate"),
        @Index(name = "idx_inv_customer", columnList = "customerID")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer invoiceID;

    @Column(nullable = false, unique = true, length = 30)
    private String invoiceNo;

    @Column(nullable = false)
    private LocalDate invoiceDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerID")
    private Customer customer;

    @Column(nullable = false, length = 200)
    private String customerName;

    @Column(length = 15)
    private String customerGstin;

    @Column(length = 100)
    private String customerState;

    @Column(length = 100)
    private String placeOfSupply;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cgst = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sgst = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal igst = BigDecimal.ZERO;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal roundOff = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    private String paymentMode = "CASH";

    @Column(nullable = false, length = 20)
    private String status = "PAID";

    private Integer createdBy;

    @Column(updatable = false)
    private LocalDateTime createdOn;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

    public void addItem(InvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
    }

    @PrePersist
    void onCreate() { this.createdOn = LocalDateTime.now(); }
}
