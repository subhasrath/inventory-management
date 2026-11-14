package com.subhas.inventory_management.controllers;

import com.subhas.inventory_management.dtos.BatchDto;
import com.subhas.inventory_management.dtos.DashboardDto;
import com.subhas.inventory_management.entity.Batch;
import com.subhas.inventory_management.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Transactional(readOnly = true)
    public DashboardDto get() {
        var today = dashboardService.todaySummary();

        List<BatchDto> expiring = dashboardService.expiringBatches().stream()
                .map(b -> BatchDto.builder()
                        .batchID(b.getBatchID())
                        .productID(b.getProduct().getProductID())
                        .itemName(b.getProduct().getItemName())
                        .batchNo(b.getBatchNo())
                        .expiryDate(b.getExpiryDate())
                        .qtyBalance(b.getQtyBalance())
                        .build())
                .collect(Collectors.toList());

        List<DashboardDto.LowStockDto> low = dashboardService.lowStockItems().stream()
                .map(ls -> DashboardDto.LowStockDto.builder()
                        .productID(ls.item().getProductID())
                        .itemName(ls.item().getItemName())
                        .currentStock(ls.totalStock())
                        .reorderLevel(ls.item().getReorderLevel())
                        .build())
                .collect(Collectors.toList());

        return DashboardDto.builder()
                .todaySales(today.sales())
                .todayInvoiceCount(today.invoiceCount())
                .expiringBatches(expiring)
                .lowStockItems(low)
                .build();
    }
}
