package com.subhas.inventory_management.dtos;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseDto {
    // Request
    private Integer vendorID;
    private String vendorBillNo;
    private LocalDate purchaseDate;
    private Integer createdBy;
    private List<PurchaseLineDto> items;

    // Response
    private Integer purchaseID;
    private String purchaseNo;
    private BigDecimal subTotal;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal grandTotal;
}
