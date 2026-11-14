package com.subhas.inventory_management.dtos;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VendorDto {
    private Integer vendorID;
    private String fullName;
    private String gstin;
    private String email;
    private String mobile;
    private String phone2;
    private String address;
    private String address2;
    private String city;
    private String district;
    private String state;
    private String pincode;
    private String status;
}
