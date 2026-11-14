package com.subhas.inventory_management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "company")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer companyID;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 15)
    private String gstin;

    @Column(nullable = false, length = 300)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(length = 10)
    private String pincode;

    @Column(length = 20)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(length = 300)
    private String logoPath;

    @Builder.Default
    @Column(nullable = false, length = 10)
    private String invoicePrefix = "INV";

    @Builder.Default
    @Column(nullable = false)
    private Integer nextInvoiceNo = 1;

    @Column(updatable = false)
    private LocalDateTime createdOn;

    @PrePersist
    void onCreate() { this.createdOn = LocalDateTime.now(); }
}