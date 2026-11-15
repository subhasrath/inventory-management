package com.subhas.inventory_management.controllers;

import com.subhas.inventory_management.dtos.PurchaseDto;
import com.subhas.inventory_management.dtos.PurchaseLineDto;
import com.subhas.inventory_management.dtos.VendorSummaryDto;
import com.subhas.inventory_management.entity.Purchase;
import com.subhas.inventory_management.entity.PurchaseItem;
import com.subhas.inventory_management.exception.ApiException;
import com.subhas.inventory_management.repository.PurchaseRepository;
import com.subhas.inventory_management.services.PurchasePdfService;
import com.subhas.inventory_management.services.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final PurchaseRepository purchaseRepository;
    private final PurchasePdfService purchasePdfService;

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

    @GetMapping("/search")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<PurchaseDto> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer vendorID,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        String query = (q != null && !q.trim().isEmpty()) ? q.trim() : null;
        LocalDate fromDate = (from != null && !from.isBlank()) ? LocalDate.parse(from) : null;
        LocalDate toDate   = (to   != null && !to.isBlank())   ? LocalDate.parse(to)   : null;

        List<Purchase> purchases = purchaseRepository.search(query, vendorID, fromDate, toDate);
        return purchases.stream().map(p -> toDto(p, true)).collect(Collectors.toList());
    }

    @GetMapping("/export/csv")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer vendorID,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        String query = (q != null && !q.trim().isEmpty()) ? q.trim() : null;
        LocalDate fromDate = (from != null && !from.isBlank()) ? LocalDate.parse(from) : null;
        LocalDate toDate   = (to   != null && !to.isBlank())   ? LocalDate.parse(to)   : null;

        List<Purchase> purchases = purchaseRepository.search(query, vendorID, fromDate, toDate);

        StringBuilder csv = new StringBuilder();
        csv.append("Purchase No,Purchase Date,Vendor,Vendor GSTIN,Bill No,Item,Batch,Expiry,Qty,Rate,GST%,Taxable,Tax,Total\n");

        java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
        for (Purchase p : purchases) {
            for (PurchaseItem pi : p.getItems()) {
                csv.append(csvCell(p.getPurchaseNo())).append(',');
                csv.append(csvCell(p.getPurchaseDate() != null ? p.getPurchaseDate().toString() : "")).append(',');
                csv.append(csvCell(p.getVendor() != null ? p.getVendor().getFullName() : "")).append(',');
                csv.append(csvCell(p.getVendor() != null ? p.getVendor().getGstin() : "")).append(',');
                csv.append(csvCell(p.getVendorBillNo())).append(',');
                csv.append(csvCell(pi.getProduct() != null ? pi.getProduct().getItemName() : "")).append(',');
                csv.append(csvCell(pi.getBatchNo())).append(',');
                csv.append(csvCell(pi.getExpiryDate() != null ? pi.getExpiryDate().toString() : "")).append(',');
                csv.append(pi.getQuantity()).append(',');
                csv.append(df.format(pi.getPurchaseRate())).append(',');
                csv.append(pi.getGstRate()).append(',');
                csv.append(df.format(pi.getTaxableValue())).append(',');
                csv.append(df.format(pi.getTaxAmount())).append(',');
                csv.append(df.format(pi.getLineTotal())).append('\n');
            }
        }

        byte[] bytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=\"vendor-purchases.csv\"")
                .body(bytes);
    }

    @GetMapping("/export/pdf")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer vendorID,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        String query = (q != null && !q.trim().isEmpty()) ? q.trim() : null;
        LocalDate fromDate = (from != null && !from.isBlank()) ? LocalDate.parse(from) : null;
        LocalDate toDate   = (to   != null && !to.isBlank())   ? LocalDate.parse(to)   : null;

        List<Purchase> purchases = purchaseRepository.search(query, vendorID, fromDate, toDate);

        byte[] pdf = purchasePdfService.generatePurchaseReport(purchases, fromDate, toDate);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"vendor-purchases.pdf\"")
                .body(pdf);
    }

    private String csvCell(String s) {
        if (s == null) return "";
        // escape quotes and commas
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    // ---------- VENDOR-WISE SUMMARY (JSON) ----------
    @GetMapping("/summary/vendors")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<VendorSummaryDto> vendorSummary(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        LocalDate fromDate = (from != null && !from.isBlank()) ? LocalDate.parse(from) : null;
        LocalDate toDate   = (to   != null && !to.isBlank())   ? LocalDate.parse(to)   : null;

        return mapVendorSummary(purchaseRepository.vendorSummary(fromDate, toDate));
    }

    // ---------- VENDOR-WISE SUMMARY (CSV) ----------
    @GetMapping("/summary/vendors/export/csv")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<byte[]> vendorSummaryCsv(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        LocalDate fromDate = (from != null && !from.isBlank()) ? LocalDate.parse(from) : null;
        LocalDate toDate   = (to   != null && !to.isBlank())   ? LocalDate.parse(to)   : null;

        List<VendorSummaryDto> rows = mapVendorSummary(purchaseRepository.vendorSummary(fromDate, toDate));

        StringBuilder csv = new StringBuilder();
        csv.append("Vendor ID,Vendor Name,GSTIN,Purchases,Taxable Value,CGST,SGST,IGST,Total Tax,Grand Total\n");

        java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
        BigDecimal sumTaxable = BigDecimal.ZERO, sumTax = BigDecimal.ZERO, sumGrand = BigDecimal.ZERO;

        for (VendorSummaryDto r : rows) {
            csv.append(csvCell(String.valueOf(r.getVendorID()))).append(',');
            csv.append(csvCell(r.getVendorName())).append(',');
            csv.append(csvCell(r.getGstin())).append(',');
            csv.append(r.getPurchaseCount()).append(',');
            csv.append(df.format(r.getSubTotal())).append(',');
            csv.append(df.format(r.getCgst())).append(',');
            csv.append(df.format(r.getSgst())).append(',');
            csv.append(df.format(r.getIgst())).append(',');
            csv.append(df.format(r.getTotalTax())).append(',');
            csv.append(df.format(r.getGrandTotal())).append('\n');

            sumTaxable = sumTaxable.add(r.getSubTotal());
            sumTax     = sumTax.add(r.getTotalTax());
            sumGrand   = sumGrand.add(r.getGrandTotal());
        }
        // totals row
        csv.append(",,,,");
        csv.append(df.format(sumTaxable)).append(",,");
        csv.append(df.format(sumTax)).append(',');
        csv.append(df.format(sumGrand)).append('\n');

        byte[] bytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=\"vendor-summary.csv\"")
                .body(bytes);
    }

    // ---------- VENDOR-WISE SUMMARY (PDF) ----------
    @GetMapping("/summary/vendors/export/pdf")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<byte[]> vendorSummaryPdf(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        LocalDate fromDate = (from != null && !from.isBlank()) ? LocalDate.parse(from) : null;
        LocalDate toDate   = (to   != null && !to.isBlank())   ? LocalDate.parse(to)   : null;

        List<VendorSummaryDto> rows = mapVendorSummary(purchaseRepository.vendorSummary(fromDate, toDate));
        byte[] pdf = purchasePdfService.generateVendorSummary(rows, fromDate, toDate);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"vendor-summary.pdf\"")
                .body(pdf);
    }

    // ---------- helper: raw Object[] → DTO ----------
    private List<VendorSummaryDto> mapVendorSummary(List<Object[]> rows) {
        List<VendorSummaryDto> out = new java.util.ArrayList<>();
        for (Object[] r : rows) {
            BigDecimal cgst = r[5] != null ? (BigDecimal) r[5] : BigDecimal.ZERO;
            BigDecimal sgst = r[6] != null ? (BigDecimal) r[6] : BigDecimal.ZERO;
            BigDecimal igst = r[7] != null ? (BigDecimal) r[7] : BigDecimal.ZERO;
            out.add(VendorSummaryDto.builder()
                    .vendorID(r[0] != null ? (Integer) r[0] : null)
                    .vendorName((String) r[1])
                    .gstin((String) r[2])
                    .purchaseCount(((Number) r[3]).longValue())
                    .subTotal(r[4] != null ? (BigDecimal) r[4] : BigDecimal.ZERO)
                    .cgst(cgst)
                    .sgst(sgst)
                    .igst(igst)
                    .totalTax(cgst.add(sgst).add(igst))
                    .grandTotal(r[8] != null ? (BigDecimal) r[8] : BigDecimal.ZERO)
                    .build());
        }
        return out;
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
