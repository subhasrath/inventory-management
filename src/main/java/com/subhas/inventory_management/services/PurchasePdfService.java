package com.subhas.inventory_management.services;

import com.subhas.inventory_management.entity.Purchase;
import com.subhas.inventory_management.entity.PurchaseItem;
import lombok.RequiredArgsConstructor;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchasePdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static final Font F_TITLE   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    private static final Font F_SUB     = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font F_LABEL   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font F_NORMAL  = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font F_SMALL   = FontFactory.getFont(FontFactory.HELVETICA, 7);
    private static final Font F_TOTAL   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

    public byte[] generatePurchaseReport(List<Purchase> purchases,
                                         LocalDate from, LocalDate to) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 20, 20, 20, 20);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            doc.open();

            PdfContentByte cb = writer.getDirectContent();
            float pageW = PageSize.A4.getWidth();
            float pageH = PageSize.A4.getHeight();

            float y = pageH - 30;

            // ---- Title ----
            drawText(cb, "Vendor Purchase History Report", 30, y, F_TITLE);
            y -= 16;
            String period = "Period: " + (from != null ? from.format(DATE_FMT) : "All") +
                    " to " + (to != null ? to.format(DATE_FMT) : "All");
            drawText(cb, period, 30, y, F_SUB);
            y -= 10;
            drawText(cb, "Generated on: " + LocalDate.now().format(DATE_FMT), 30, y, F_SMALL);
            y -= 20;

            // ---- Table header ----
            float[] colW = { 20, 55, 60, 70, 110, 28, 24, 26, 50 };
            // #, Date, Purchase No, Vendor, Item, Qty, GST%, Tax, Total
            String[] headers = { "#", "Date", "Purchase No", "Vendor", "Item", "Qty", "GST%", "Tax", "Total" };

            cb.saveState();
            cb.setGrayFill(0.9f);
            cb.rectangle(28, y - 4, pageW - 56, 14);
            cb.fill();
            cb.restoreState();

            float colX = 30;
            for (int i = 0; i < headers.length; i++) {
                drawText(cb, headers[i], colX, y, F_LABEL);
                colX += colW[i];
            }
            y -= 14;
            cb.setLineWidth(0.4f);
            cb.moveTo(28, y + 10);
            cb.lineTo(pageW - 28, y + 10);
            cb.stroke();

            // ---- Rows ----
            BigDecimal grandTotal = BigDecimal.ZERO;
            BigDecimal grandTax   = BigDecimal.ZERO;
            int rowIdx = 1;

            for (Purchase p : purchases) {
                for (PurchaseItem pi : p.getItems()) {
                    if (y < 90) {
                        // new page
                        doc.newPage();
                        cb = writer.getDirectContent();
                        y = pageH - 30;
                        // repeat header
                        cb.saveState();
                        cb.setGrayFill(0.9f);
                        cb.rectangle(28, y - 4, pageW - 56, 14);
                        cb.fill();
                        cb.restoreState();
                        colX = 30;
                        for (int i = 0; i < headers.length; i++) {
                            drawText(cb, headers[i], colX, y, F_LABEL);
                            colX += colW[i];
                        }
                        y -= 14;
                    }

                    colX = 30;
                    String[] vals = {
                            String.valueOf(rowIdx++),
                            p.getPurchaseDate() != null ? p.getPurchaseDate().format(DATE_FMT) : "-",
                            safe(p.getPurchaseNo()),
                            truncate(safe(p.getVendor() != null ? p.getVendor().getFullName() : ""), 18),
                            truncate(safe(pi.getProduct() != null ? pi.getProduct().getItemName() : ""), 28),
                            stripZeros(pi.getQuantity()),
                            stripZeros(pi.getGstRate()) + "%",
                            money(pi.getTaxAmount()),
                            money(pi.getLineTotal())
                    };
                    for (int i = 0; i < vals.length; i++) {
                        drawText(cb, vals[i], colX, y, F_SMALL);
                        colX += colW[i];
                    }
                    grandTotal = grandTotal.add(pi.getLineTotal());
                    grandTax   = grandTax.add(pi.getTaxAmount());
                    y -= 10;
                }
            }

            // ---- Totals ----
            y -= 6;
            cb.setLineWidth(0.4f);
            cb.moveTo(pageW - 250, y);
            cb.lineTo(pageW - 30, y);
            cb.stroke();
            y -= 12;
            drawText(cb, "Total Tax:", pageW - 250, y, F_NORMAL);
            drawText(cb, "Rs. " + money(grandTax), pageW - 130, y, F_NORMAL);
            y -= 10;
            drawText(cb, "Grand Total:", pageW - 250, y, F_TOTAL);
            drawText(cb, "Rs. " + money(grandTotal), pageW - 130, y, F_TOTAL);

            // ---- Footer ----
            y = 30;
            drawText(cb, "Generated by Inventory Management System",
                    30, y, F_SMALL);

            doc.close();
            return out.toByteArray();

        } catch (DocumentException | java.io.IOException ex) {
            throw new RuntimeException("Failed to generate purchase report: " + ex.getMessage(), ex);
        }
    }

    // ---- helpers ----
    private void drawText(PdfContentByte cb, String text, float x, float y, Font font) {
        cb.beginText();
        cb.setFontAndSize(font.getBaseFont(), font.getSize());
        cb.setTextMatrix(x, y);
        cb.showText(text != null ? text : "");
        cb.endText();
    }

    private String money(BigDecimal v) {
        if (v == null) return "0.00";
        return v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private String stripZeros(BigDecimal v) {
        if (v == null) return "0";
        return v.stripTrailingZeros().toPlainString();
    }

    private String safe(String s) { return s == null ? "" : s; }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    public byte[] generateVendorSummary(List<com.subhas.inventory_management.dtos.VendorSummaryDto> rows,
                                        LocalDate from, LocalDate to) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 20, 20, 20, 20);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            doc.open();

            PdfContentByte cb = writer.getDirectContent();
            float pageW = PageSize.A4.getWidth();
            float pageH = PageSize.A4.getHeight();
            float y = pageH - 30;

            // ---- Title ----
            drawText(cb, "Vendor-wise Purchase Summary", 30, y, F_TITLE);
            y -= 16;
            String period = "Period: " + (from != null ? from.format(DATE_FMT) : "All") +
                    " to " + (to != null ? to.format(DATE_FMT) : "All");
            drawText(cb, period, 30, y, F_SUB);
            y -= 10;
            drawText(cb, "Generated on: " + LocalDate.now().format(DATE_FMT), 30, y, F_SMALL);
            y -= 20;

            // ---- Table header ----
            float[] colW = { 20, 130, 90, 40, 60, 50, 60 };
            // #, Vendor, GSTIN, Purchases, Taxable, Tax, Grand Total
            String[] headers = { "#", "Vendor", "GSTIN", "Purchases", "Taxable", "Tax (ITC)", "Grand Total" };

            cb.saveState();
            cb.setGrayFill(0.9f);
            cb.rectangle(28, y - 4, pageW - 56, 14);
            cb.fill();
            cb.restoreState();

            float colX = 30;
            for (int i = 0; i < headers.length; i++) {
                drawText(cb, headers[i], colX, y, F_LABEL);
                colX += colW[i];
            }
            y -= 14;
            cb.setLineWidth(0.4f);
            cb.moveTo(28, y + 10);
            cb.lineTo(pageW - 28, y + 10);
            cb.stroke();

            // ---- Rows ----
            BigDecimal sumTaxable = BigDecimal.ZERO;
            BigDecimal sumTax     = BigDecimal.ZERO;
            BigDecimal sumGrand   = BigDecimal.ZERO;
            int idx = 1;

            for (var r : rows) {
                if (y < 90) {
                    doc.newPage();
                    cb = writer.getDirectContent();
                    y = pageH - 30;
                    cb.saveState();
                    cb.setGrayFill(0.9f);
                    cb.rectangle(28, y - 4, pageW - 56, 14);
                    cb.fill();
                    cb.restoreState();
                    colX = 30;
                    for (int i = 0; i < headers.length; i++) {
                        drawText(cb, headers[i], colX, y, F_LABEL);
                        colX += colW[i];
                    }
                    y -= 14;
                }

                colX = 30;
                String[] vals = {
                        String.valueOf(idx++),
                        truncate(safe(r.getVendorName()), 30),
                        safe(r.getGstin()),
                        String.valueOf(r.getPurchaseCount()),
                        money(r.getSubTotal()),
                        money(r.getTotalTax()),
                        money(r.getGrandTotal())
                };
                for (int i = 0; i < vals.length; i++) {
                    drawText(cb, vals[i], colX, y, F_SMALL);
                    colX += colW[i];
                }
                sumTaxable = sumTaxable.add(r.getSubTotal());
                sumTax     = sumTax.add(r.getTotalTax());
                sumGrand   = sumGrand.add(r.getGrandTotal());
                y -= 10;
            }

            // ---- Totals ----
            y -= 6;
            cb.setLineWidth(0.6f);
            cb.moveTo(pageW - 310, y);
            cb.lineTo(pageW - 30, y);
            cb.stroke();
            y -= 12;
            drawText(cb, "Total Taxable:", pageW - 310, y, F_LABEL);
            drawText(cb, "Rs. " + money(sumTaxable), pageW - 180, y, F_NORMAL);
            y -= 10;
            drawText(cb, "Total ITC (Tax):", pageW - 310, y, F_LABEL);
            drawText(cb, "Rs. " + money(sumTax), pageW - 180, y, F_NORMAL);
            y -= 12;
            drawText(cb, "Grand Total:", pageW - 310, y, F_TOTAL);
            drawText(cb, "Rs. " + money(sumGrand), pageW - 180, y, F_TOTAL);

            // ---- Footer ----
            drawText(cb, "Generated by Inventory Management System", 30, 30, F_SMALL);
            drawText(cb, "Total ITC claim: Rs. " + money(sumTax), 30, 20, F_SMALL);

            doc.close();
            return out.toByteArray();

        } catch (DocumentException | java.io.IOException ex) {
            throw new RuntimeException("Failed to generate vendor summary PDF: " + ex.getMessage(), ex);
        }
    }
}