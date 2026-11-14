package com.subhas.inventory_management.dtos;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InvoiceLineDto {
    // Request
    private Integer productID;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;

    // Response
    private Integer invoiceItemID;
    private Integer batchID;
    private String itemName;
    private String batchNo;
    private LocalDate expiryDate;
    private String hsnCode;
    private BigDecimal gstRate;
    private BigDecimal taxableValue;
    private BigDecimal taxAmount;
    private BigDecimal lineTotal;
}
