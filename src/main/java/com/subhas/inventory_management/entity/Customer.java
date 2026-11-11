package com.subhas.inventory_management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer", indexes = {
        @Index(name = "idx_cust_name", columnList = "fullName"),
        @Index(name = "idx_cust_gstin", columnList = "gstin")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerID;

    @Column(nullable = false, length = 200)
    private String fullName;

    @Column(length = 15)
    private String gstin;

    @Column(nullable = false, length = 15)
    private String mobile;

    @Column(length = 15)
    private String phone2;

    @Column(length = 150)
    private String email;

    @Column(nullable = false, length = 300)
    private String address;

    @Column(length = 300)
    private String address2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String district;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(length = 10)
    private String pincode;

    @Column(nullable = false, length = 20)
    private String status = "Active";

    @Column(updatable = false)
    private LocalDateTime createdOn;

    @PrePersist
    void onCreate() { this.createdOn = LocalDateTime.now(); }
}
