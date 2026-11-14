package com.subhas.inventory_management.dtos;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardDto {
    private BigDecimal todaySales;
    private long todayInvoiceCount;
    private List<BatchDto> expiringBatches;
    private List<LowStockDto> lowStockItems;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LowStockDto {
        private Integer productID;
        private String itemName;
        private BigDecimal currentStock;
        private Integer reorderLevel;
    }
}
