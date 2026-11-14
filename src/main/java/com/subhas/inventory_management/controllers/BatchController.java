package com.subhas.inventory_management.controllers;

import com.subhas.inventory_management.dtos.BatchDto;
import com.subhas.inventory_management.entity.Batch;
import com.subhas.inventory_management.repository.BatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchRepository repo;

    /** All batches for one product */
    @GetMapping("/product/{productID}")
    public List<BatchDto> byProduct(@PathVariable Integer productID) {
        return repo.findByProductProductIDOrderByExpiryDateAsc(productID)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    /** Batches expiring within N days (default 90) */
    @GetMapping("/expiring")
    public List<BatchDto> expiring(@RequestParam(defaultValue = "90") int days) {
        return repo.findExpiringBefore(LocalDate.now().plusDays(days))
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    private BatchDto toDto(Batch b) {
        return BatchDto.builder()
                .batchID(b.getBatchID())
                .productID(b.getProduct().getProductID())
                .itemName(b.getProduct().getItemName())
                .batchNo(b.getBatchNo())
                .expiryDate(b.getExpiryDate())
                .mrp(b.getMrp())
                .purchaseRate(b.getPurchaseRate())
                .saleRate(b.getSaleRate())
                .qtyIn(b.getQtyIn())
                .qtyBalance(b.getQtyBalance())
                .vendorID(b.getVendor() != null ? b.getVendor().getVendorID() : null)
                .vendorName(b.getVendor() != null ? b.getVendor().getFullName() : null)
                .purchaseDate(b.getPurchaseDate())
                .build();
    }
}
