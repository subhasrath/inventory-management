package com.subhas.inventory_management.dtos;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InvoiceDto {

    // ---- Request fields ----
    private Integer customerID;               // null for walk-in
    private String customerName;              // used when customerID is null
    private String customerGstin;
    private String customerState;
    private String paymentMode;               // CASH / UPI / CARD / CREDIT
    private Integer createdBy;
    private List<InvoiceLineDto> items;

    // ---- Response fields (populated on read) ----
    private Integer invoiceID;
    private String invoiceNo;
    private LocalDate invoiceDate;
    private String placeOfSupply;
    private BigDecimal subTotal;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal roundOff;
    private BigDecimal grandTotal;
    private String status;
}