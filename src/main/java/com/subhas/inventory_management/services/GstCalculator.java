package com.subhas.inventory_management.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Computes CGST/SGST (intra-state) or IGST (inter-state) per line,
 * plus invoice totals with rupee rounding.
 */
@Service
public class GstCalculator {

    @Value("${app.company.state:Maharashtra}")
    private String companyState;

    public record LineTax(BigDecimal taxable, BigDecimal tax) {}
    public record InvoiceTotals(
            BigDecimal subTotal, BigDecimal cgst, BigDecimal sgst,
            BigDecimal igst, BigDecimal roundOff, BigDecimal grandTotal) {}

    /** taxable = (unitPrice - discount%) * qty */
    public LineTax computeLine(BigDecimal unitPrice, BigDecimal discountPct,
                               BigDecimal qty, BigDecimal gstRate) {
        BigDecimal gross = unitPrice.multiply(qty);
        BigDecimal discountAmt = gross.multiply(discountPct)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal taxable = gross.subtract(discountAmt)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = taxable.multiply(gstRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        return new LineTax(taxable, tax);
    }

    public boolean isInterState(String customerState) {
        if (customerState == null) return false;
        return !customerState.trim().equalsIgnoreCase(companyState);
    }

    /** Aggregate line totals into invoice header values. */
    public InvoiceTotals aggregate(java.util.List<LineTax> lines,
                                   java.util.List<BigDecimal> gstRates,
                                   String customerState) {
        BigDecimal sub = BigDecimal.ZERO, tax = BigDecimal.ZERO;
        for (LineTax lt : lines) {
            sub = sub.add(lt.taxable());
            tax = tax.add(lt.tax());
        }
        boolean inter = isInterState(customerState);
        BigDecimal cgst = inter ? BigDecimal.ZERO : tax.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
        BigDecimal sgst = inter ? BigDecimal.ZERO : tax.subtract(cgst);
        BigDecimal igst = inter ? tax : BigDecimal.ZERO;

        BigDecimal beforeRound = sub.add(cgst).add(sgst).add(igst);
        BigDecimal rounded = beforeRound.setScale(0, RoundingMode.HALF_UP)
                .setScale(2);
        BigDecimal roundOff = rounded.subtract(beforeRound);

        return new InvoiceTotals(sub, cgst, sgst, igst, roundOff, rounded);
    }
}
