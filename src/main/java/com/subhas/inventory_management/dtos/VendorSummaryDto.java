package com.subhas.inventory_management.dtos;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VendorSummaryDto {
    private Integer vendorID;
    private String vendorName;
    private String gstin;
    private Long purchaseCount;
    private BigDecimal subTotal;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal totalTax;
    private BigDecimal grandTotal;
}