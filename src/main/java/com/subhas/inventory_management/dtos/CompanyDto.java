package com.subhas.inventory_management.dtos;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompanyDto {
    private Integer companyID;
    private String name;
    private String gstin;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String phone;
    private String email;
    private String logoPath;
    private String invoicePrefix;
    private Integer nextInvoiceNo;
}