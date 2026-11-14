package com.subhas.inventory_management.dtos;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BatchDto {
    private Integer batchID;
    private Integer productID;
    private String itemName;
    private String batchNo;
    private LocalDate expiryDate;
    private BigDecimal mrp;
    private BigDecimal purchaseRate;
    private BigDecimal saleRate;
    private BigDecimal qtyIn;
    private BigDecimal qtyBalance;
    private Integer vendorID;
    private String vendorName;
    private LocalDate purchaseDate;
}
