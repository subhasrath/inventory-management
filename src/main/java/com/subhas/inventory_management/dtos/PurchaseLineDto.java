package com.subhas.inventory_management.dtos;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseLineDto {
    // Request
    private Integer productID;
    private String batchNo;
    private LocalDate expiryDate;
    private BigDecimal mrp;
    private BigDecimal quantity;
    private BigDecimal purchaseRate;
    private BigDecimal gstRate;

    // Response
    private Integer purchaseItemID;
    private Integer batchID;
    private BigDecimal taxableValue;
    private BigDecimal taxAmount;
    private BigDecimal lineTotal;
}
