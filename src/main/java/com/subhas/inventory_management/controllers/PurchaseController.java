package com.subhas.inventory_management.controllers;

import com.subhas.inventory_management.dtos.PurchaseDto;
import com.subhas.inventory_management.dtos.PurchaseLineDto;
import com.subhas.inventory_management.entity.Purchase;
import com.subhas.inventory_management.entity.PurchaseItem;
import com.subhas.inventory_management.exception.ApiException;
import com.subhas.inventory_management.repository.PurchaseRepository;
import com.subhas.inventory_management.services.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final PurchaseRepository purchaseRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<PurchaseDto> create(@RequestBody PurchaseDto dto) {
        List<PurchaseService.ReceiveLine> lines = new ArrayList<>();
        for (PurchaseLineDto l : dto.getItems()) {
            lines.add(new PurchaseService.ReceiveLine(
                    l.getProductID(), l.getBatchNo(), l.getExpiryDate(),
                    l.getMrp(), l.getQuantity(), l.getPurchaseRate(), l.getGstRate()));
        }
        Purchase saved = purchaseService.receive(
                dto.getVendorID(), dto.getVendorBillNo(),
                dto.getPurchaseDate(), lines, dto.getCreatedBy());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved, true));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<PurchaseDto> list() {
        return purchaseRepository.findAll().stream()
                .map(p -> toDto(p, false)).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PurchaseDto get(@PathVariable Integer id) {
        Purchase p = purchaseRepository.findById(id)
                .orElseThrow(() -> new ApiException("Purchase not found", HttpStatus.NOT_FOUND));
        return toDto(p, true);
    }

    private PurchaseDto toDto(Purchase p, boolean includeItems) {
        PurchaseDto dto = PurchaseDto.builder()
                .purchaseID(p.getPurchaseID())
                .purchaseNo(p.getPurchaseNo())
                .purchaseDate(p.getPurchaseDate())
                .vendorID(p.getVendor() != null ? p.getVendor().getVendorID() : null)
                .vendorBillNo(p.getVendorBillNo())
                .subTotal(p.getSubTotal())
                .cgst(p.getCgst()).sgst(p.getSgst()).igst(p.getIgst())
                .grandTotal(p.getGrandTotal())
                .build();

        if (includeItems && p.getItems() != null) {
            dto.setItems(p.getItems().stream().map(it -> PurchaseLineDto.builder()
                    .purchaseItemID(it.getPurchaseItemID())
                    .batchID(it.getBatch() != null ? it.getBatch().getBatchID() : null)
                    .productID(it.getProduct().getProductID())
                    .batchNo(it.getBatchNo())
                    .expiryDate(it.getExpiryDate())
                    .mrp(it.getMrp())
                    .quantity(it.getQuantity())
                    .purchaseRate(it.getPurchaseRate())
                    .gstRate(it.getGstRate())
                    .taxableValue(it.getTaxableValue())
                    .taxAmount(it.getTaxAmount())
                    .lineTotal(it.getLineTotal())
                    .build()).collect(Collectors.toList()));
        }
        return dto;
    }
}
