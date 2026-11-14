package com.subhas.inventory_management.controllers;

import com.subhas.inventory_management.dtos.InvoiceDto;
import com.subhas.inventory_management.dtos.InvoiceLineDto;
import com.subhas.inventory_management.entity.*;
import com.subhas.inventory_management.exception.ApiException;
import com.subhas.inventory_management.repository.CustomerRepository;
import com.subhas.inventory_management.repository.InvoiceRepository;
import com.subhas.inventory_management.repository.ItemRepository;
import com.subhas.inventory_management.services.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ItemRepository itemRepository;

    // ---------- CREATE ----------
    @PostMapping
    @Transactional
    public ResponseEntity<InvoiceDto> create(@RequestBody InvoiceDto dto) {
        Customer customer = null;
        if (dto.getCustomerID() != null) {
            customer = customerRepository.findById(dto.getCustomerID())
                    .orElseThrow(() -> new ApiException("Customer not found", HttpStatus.NOT_FOUND));
        } else if (dto.getCustomerName() != null) {
            // Walk-in: build a transient Customer object (not persisted)
            customer = Customer.builder()
                    .fullName(dto.getCustomerName())
                    .gstin(dto.getCustomerGstin())
                    .state(dto.getCustomerState())
                    .build();
        }

        List<InvoiceService.SaleLine> saleLines = new ArrayList<>();
        for (InvoiceLineDto l : dto.getItems()) {
            Item item = itemRepository.findById(l.getProductID())
                    .orElseThrow(() -> new ApiException("Item not found: " + l.getProductID(), HttpStatus.NOT_FOUND));
            saleLines.add(new InvoiceService.SaleLine(
                    item,
                    l.getQuantity(),
                    l.getUnitPrice() != null ? l.getUnitPrice() : item.getUnitPrice(),
                    l.getDiscount() != null ? l.getDiscount() : item.getDiscount()
            ));
        }

        Invoice saved = invoiceService.createInvoice(
                customer, saleLines,
                dto.getPaymentMode() != null ? dto.getPaymentMode() : "CASH",
                dto.getCreatedBy());

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved, true));
    }

    // ---------- LIST / SEARCH ----------
    @GetMapping
    public List<InvoiceDto> list(@RequestParam(required = false) String from,
                                 @RequestParam(required = false) String to) {
        List<Invoice> invoices;
        if (from != null && to != null) {
            invoices = invoiceRepository.findByInvoiceDateBetween(
                    LocalDate.parse(from), LocalDate.parse(to));
        } else {
            invoices = invoiceRepository.findAll();
        }
        return invoices.stream().map(i -> toDto(i, false)).collect(Collectors.toList());
    }

    // ---------- GET ONE (with line items) ----------
    @GetMapping("/{id}")
    public InvoiceDto get(@PathVariable Integer id) {
        Invoice inv = invoiceRepository.findById(id)
                .orElseThrow(() -> new ApiException("Invoice not found", HttpStatus.NOT_FOUND));
        return toDto(inv, true);
    }

    @GetMapping("/by-no/{invoiceNo}")
    public InvoiceDto getByNo(@PathVariable String invoiceNo) {
        Invoice inv = invoiceRepository.findByInvoiceNo(invoiceNo)
                .orElseThrow(() -> new ApiException("Invoice not found", HttpStatus.NOT_FOUND));
        return toDto(inv, true);
    }

    // ---------- Helper: map entity → DTO ----------
    private InvoiceDto toDto(Invoice inv, boolean includeItems) {
        InvoiceDto dto = InvoiceDto.builder()
                .invoiceID(inv.getInvoiceID())
                .invoiceNo(inv.getInvoiceNo())
                .invoiceDate(inv.getInvoiceDate())
                .customerID(inv.getCustomer() != null ? inv.getCustomer().getCustomerID() : null)
                .customerName(inv.getCustomerName())
                .customerGstin(inv.getCustomerGstin())
                .customerState(inv.getCustomerState())
                .placeOfSupply(inv.getPlaceOfSupply())
                .subTotal(inv.getSubTotal())
                .cgst(inv.getCgst())
                .sgst(inv.getSgst())
                .igst(inv.getIgst())
                .roundOff(inv.getRoundOff())
                .grandTotal(inv.getGrandTotal())
                .paymentMode(inv.getPaymentMode())
                .status(inv.getStatus())
                .createdBy(inv.getCreatedBy())
                .build();

        if (includeItems && inv.getItems() != null) {
            dto.setItems(inv.getItems().stream().map(it -> InvoiceLineDto.builder()
                    .invoiceItemID(it.getInvoiceItemID())
                    .batchID(it.getBatch() != null ? it.getBatch().getBatchID() : null)
                    .productID(it.getProduct().getProductID())
                    .itemName(it.getItemName())
                    .batchNo(it.getBatchNo())
                    .expiryDate(it.getExpiryDate())
                    .hsnCode(it.getHsnCode())
                    .quantity(it.getQuantity())
                    .unitPrice(it.getUnitPrice())
                    .discount(it.getDiscount())
                    .gstRate(it.getGstRate())
                    .taxableValue(it.getTaxableValue())
                    .taxAmount(it.getTaxAmount())
                    .lineTotal(it.getLineTotal())
                    .build()).collect(Collectors.toList()));
        }
        return dto;
    }
}
