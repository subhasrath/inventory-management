package com.subhas.inventory_management.dtos;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemDto {
    private Integer productID;
    private String itemNumber;
    private String barcode;
    private String itemName;
    private String description;
    private String hsnCode;
    private BigDecimal gstRate;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private Integer reorderLevel;
    private String imageURL;
    private String status;
    private BigDecimal totalStock;   // computed from batches
}
