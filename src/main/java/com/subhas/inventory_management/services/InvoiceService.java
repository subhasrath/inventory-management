package com.subhas.inventory_management.services;

import com.subhas.inventory_management.entity.*;
import com.subhas.inventory_management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CompanyRepository companyRepository;
    private final BatchAllocationService allocationService;
    private final GstCalculator gst;

    public record SaleLine(Item product, BigDecimal qty, BigDecimal unitPrice,
                           BigDecimal discountPct) {}

    @Transactional
    public Invoice createInvoice(Customer customer, List<SaleLine> lines,
                                 String paymentMode, Integer userId) {

        Company company = companyRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Company not configured"));

        Invoice invoice = Invoice.builder()
                .invoiceNo(nextInvoiceNo(company))
                .invoiceDate(LocalDate.now())
                .customer(customer)
                .customerName(customer != null ? customer.getFullName() : "Walk-in")
                .customerGstin(customer != null ? customer.getGstin() : null)
                .customerState(customer != null ? customer.getState() : company.getState())
                .placeOfSupply(customer != null ? customer.getState() : company.getState())
                .paymentMode(paymentMode)
                .createdBy(userId)
                .build();

        List<GstCalculator.LineTax> taxLines = new ArrayList<>();
        List<BigDecimal> rates = new ArrayList<>();
        List<BatchAllocationService.Allocation> pendingDeductions = new ArrayList<>();
        List<InvoiceItem> itemEntities = new ArrayList<>();

        for (SaleLine line : lines) {
            var allocs = allocationService.allocate(line.product().getProductID(), line.qty());
            for (var alloc : allocs) {
                var lt = gst.computeLine(line.unitPrice(), line.discountPct(),
                        alloc.qty(), line.product().getGstRate());
                taxLines.add(lt);
                rates.add(line.product().getGstRate());

                itemEntities.add(InvoiceItem.builder()
                        .product(line.product())
                        .batch(alloc.batch())
                        .itemName(line.product().getItemName())
                        .batchNo(alloc.batch().getBatchNo())
                        .expiryDate(alloc.batch().getExpiryDate())
                        .hsnCode(line.product().getHsnCode())
                        .quantity(alloc.qty())
                        .unitPrice(line.unitPrice())
                        .discount(line.discountPct())
                        .gstRate(line.product().getGstRate())
                        .taxableValue(lt.taxable())
                        .taxAmount(lt.tax())
                        .lineTotal(lt.taxable().add(lt.tax()))
                        .build());

                pendingDeductions.add(alloc);
            }
        }

        var totals = gst.aggregate(taxLines, rates, invoice.getCustomerState());
        invoice.setSubTotal(totals.subTotal());
        invoice.setCgst(totals.cgst());
        invoice.setSgst(totals.sgst());
        invoice.setIgst(totals.igst());
        invoice.setRoundOff(totals.roundOff());
        invoice.setGrandTotal(totals.grandTotal());

        itemEntities.forEach(invoice::addItem);
        Invoice saved = invoiceRepository.save(invoice);

        // Deduct batches after invoice is persisted (so refID exists)
        for (var alloc : pendingDeductions) {
            allocationService.deduct(alloc, saved.getInvoiceID(), alloc.batch().getProduct());
        }

        // bump counter
        company.setNextInvoiceNo(company.getNextInvoiceNo() + 1);
        companyRepository.save(company);

        return saved;
    }

    private String nextInvoiceNo(Company c) {
        int year = LocalDate.now().getYear();
        return String.format("%s-%d-%04d", c.getInvoicePrefix(), year, c.getNextInvoiceNo());
    }
}