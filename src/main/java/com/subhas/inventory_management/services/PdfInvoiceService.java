package com.subhas.inventory_management.services;

import com.subhas.inventory_management.entity.Company;
import com.subhas.inventory_management.entity.Invoice;
import com.subhas.inventory_management.entity.InvoiceItem;
import com.subhas.inventory_management.exception.ApiException;
import com.subhas.inventory_management.repository.CompanyRepository;
import com.subhas.inventory_management.repository.InvoiceRepository;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfInvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CompanyRepository companyRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static final Font F_TITLE    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font F_COMPANY  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font F_LABEL    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
    private static final Font F_NORMAL   = FontFactory.getFont(FontFactory.HELVETICA, 7);
    private static final Font F_SMALL    = FontFactory.getFont(FontFactory.HELVETICA, 6);
    private static final Font F_TOTAL    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font F_COPYTAG  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);

    /**
     * Generates a single A4 PDF containing two identical invoice halves:
     * top = CUSTOMER COPY, bottom = DEALER COPY, dashed cut line in middle.
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public byte[] generate(Integer invoiceID) {
        Invoice invoice = invoiceRepository.findByIdWithItemsAndProducts(invoiceID)
                .orElseThrow(() -> new ApiException("Invoice not found: " + invoiceID, HttpStatus.NOT_FOUND));
        Company company = companyRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ApiException(
                        "Company details not configured. Please complete Settings → Company first.",
                        HttpStatus.PRECONDITION_REQUIRED));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 10, 10, 10, 10);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            doc.open();

            float pageW = PageSize.A4.getWidth();      // 595
            float pageH = PageSize.A4.getHeight();     // 842
            float halfH = pageH / 2f;                  // 421

            // ---- TOP HALF: CUSTOMER COPY ----
            drawCopy(writer.getDirectContent(), company, invoice, invoice.getItems(),
                    10, halfH + 5, pageW - 20, halfH - 10, "CUSTOMER COPY");

            // ---- DASHED CUT LINE ----
            PdfContentByte cb = writer.getDirectContent();
            cb.saveState();
            cb.setLineDash(3f, 3f);
            cb.setLineWidth(0.5f);
            cb.moveTo(10, halfH);
            cb.lineTo(pageW - 10, halfH);
            cb.stroke();
            cb.restoreState();

            // ---- BOTTOM HALF: DEALER COPY ----
            drawCopy(cb, company, invoice, invoice.getItems(),
                    10, 10, pageW - 20, halfH - 10, "DEALER / OFFICE COPY");

            doc.close();
            return out.toByteArray();

        } catch (DocumentException | java.io.IOException ex) {
            throw new RuntimeException("Failed to generate invoice PDF: " + ex.getMessage(), ex);
        }
    }

    /**
     * Draws one full invoice inside the given rectangle.
     * (x, y) = bottom-left corner; w = width, h = height.
     */
    private void drawCopy(PdfContentByte cb, Company company, Invoice invoice,
                          List<InvoiceItem> items,
                          float x, float y, float w, float h, String copyLabel) {

        float yTop   = y + h;
        float yCursor = yTop - 12;

        // ---- Outer border ----
        cb.saveState();
        cb.setLineWidth(0.6f);
        cb.rectangle(x, y, w, h);
        cb.stroke();
        cb.restoreState();

        // ---- Company header block (left) + copy label (right) ----
        drawText(cb, company.getName(), x + 4, yCursor, F_COMPANY);
        drawText(cb, "GSTIN: " + safe(company.getGstin()), x + 4, yCursor - 11, F_SMALL);
        drawText(cb, "GST INVOICE", x + w - 85, yCursor, F_TITLE);
        drawText(cb, copyLabel, x + w - 85, yCursor - 11, F_COPYTAG);

        yCursor -= 18;                                        // <- Level-0 fix earlier
        drawText(cb, safe(company.getAddress()) + ", " + safe(company.getCity())
                        + " - " + safe(company.getPincode()) + ", " + safe(company.getState()),
                x + 4, yCursor, F_SMALL);
        yCursor -= 9;
        drawText(cb, "Ph: " + safe(company.getPhone()) + "   Email: " + safe(company.getEmail()),
                x + 4, yCursor, F_SMALL);

        // separator
        yCursor -= 4;
        cb.setLineWidth(0.4f);
        cb.moveTo(x + 2, yCursor);
        cb.lineTo(x + w - 2, yCursor);
        cb.stroke();
        yCursor -= 10;

        // ---- Invoice meta (left) + customer (right) ----
        float leftX  = x + 4;
        float rightX = x + w * 0.55f;

        drawText(cb, "Invoice No: ", leftX, yCursor, F_LABEL);
        drawText(cb, safe(invoice.getInvoiceNo()), leftX + 42, yCursor, F_NORMAL);

        drawText(cb, "Customer: ", rightX, yCursor, F_LABEL);
        drawText(cb, safe(invoice.getCustomerName()), rightX + 42, yCursor, F_NORMAL);

        yCursor -= 9;
        drawText(cb, "Date: ", leftX, yCursor, F_LABEL);
        drawText(cb, invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().format(DATE_FMT) : "-",
                leftX + 42, yCursor, F_NORMAL);

        drawText(cb, "GSTIN: ", rightX, yCursor, F_LABEL);
        drawText(cb, safe(invoice.getCustomerGstin()), rightX + 42, yCursor, F_NORMAL);

        yCursor -= 9;
        drawText(cb, "Payment: ", leftX, yCursor, F_LABEL);
        drawText(cb, safe(invoice.getPaymentMode()), leftX + 42, yCursor, F_NORMAL);

        drawText(cb, "State: ", rightX, yCursor, F_LABEL);
        drawText(cb, safe(invoice.getCustomerState()), rightX + 42, yCursor, F_NORMAL);

        yCursor -= 9;
        drawText(cb, "Place of Supply: ", leftX, yCursor, F_LABEL);
        drawText(cb, safe(invoice.getPlaceOfSupply()), leftX + 56, yCursor, F_NORMAL);

        // separator
        yCursor -= 4;
        cb.setLineWidth(0.4f);
        cb.moveTo(x + 2, yCursor);
        cb.lineTo(x + w - 2, yCursor);
        cb.stroke();
        yCursor -= 2;

        // ---- Items table (9 columns now: #, Item, Batch, Exp, Qty, Rate, Disc, GST%, Amount) ----
        float[] colWidths = { 16, 108, 40, 38, 24, 34, 26, 24, 44 };   // sum ≈ 354
        float tableW = 0;
        for (float c : colWidths) tableW += c;
        float scale = (w - 8) / tableW;
        for (int i = 0; i < colWidths.length; i++) colWidths[i] *= scale;

        float rowH = 11f;
        float headerH = 12f;

        // header background
        cb.saveState();
        cb.setGrayFill(0.9f);
        cb.rectangle(x + 4, yCursor - headerH, w - 8, headerH);
        cb.fill();
        cb.restoreState();

        String[] headers = { "#", "Item", "Batch", "Exp", "Qty", "Rate", "Disc", "GST%", "Amount" };
        float colX = x + 4;
        for (int i = 0; i < headers.length; i++) {
            drawText(cb, headers[i], colX + 2, yCursor - headerH + 3.5f, F_LABEL);
            colX += colWidths[i];
        }
        yCursor -= headerH;

        cb.setLineWidth(0.3f);
        cb.moveTo(x + 4, yCursor);
        cb.lineTo(x + w - 4, yCursor);
        cb.stroke();

        // rows
        int rowIdx = 1;
        for (InvoiceItem it : items) {
            if (yCursor - rowH < y + 65) {           // reserve room for totals + signatures
                drawText(cb, "... continued ...", x + 8, yCursor - 8, F_SMALL);
                break;
            }
            float rowBottom = yCursor - rowH;
            colX = x + 4;

            BigDecimal discount = it.getDiscount() != null ? it.getDiscount() : BigDecimal.ZERO;
            String discStr = discount.compareTo(BigDecimal.ZERO) > 0
                    ? stripTrailingZeros(discount) + "%"
                    : "-";

            String[] vals = {
                    String.valueOf(rowIdx++),
                    truncate(safe(it.getItemName()), 22),        // shorter to fit Disc column
                    safe(it.getBatchNo()),
                    it.getExpiryDate() != null ? it.getExpiryDate().format(DATE_FMT) : "-",
                    stripTrailingZeros(it.getQuantity()),
                    money(it.getUnitPrice()),
                    discStr,                                     // <- Level 1: NEW column
                    stripTrailingZeros(it.getGstRate()) + "%",
                    money(it.getLineTotal())
            };
            for (int i = 0; i < vals.length; i++) {
                drawText(cb, vals[i], colX + 2, rowBottom + 3.5f, F_SMALL);
                colX += colWidths[i];
            }
            yCursor = rowBottom;

            cb.setLineWidth(0.15f);
            cb.setGrayStroke(0.85f);
            cb.moveTo(x + 4, yCursor);
            cb.lineTo(x + w - 4, yCursor);
            cb.stroke();
            cb.setGrayStroke(0f);
        }

        // ---- Totals block (right side) ----
        // Level 2: compute gross (pre-discount taxable) and total discount
        BigDecimal grossTaxable = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        for (InvoiceItem it : items) {
            BigDecimal qty   = it.getQuantity()   != null ? it.getQuantity()   : BigDecimal.ZERO;
            BigDecimal price = it.getUnitPrice()  != null ? it.getUnitPrice()  : BigDecimal.ZERO;
            BigDecimal disc  = it.getDiscount()   != null ? it.getDiscount()   : BigDecimal.ZERO;
            BigDecimal gross = price.multiply(qty);
            BigDecimal dAmt  = gross.multiply(disc).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            grossTaxable  = grossTaxable.add(gross);
            totalDiscount = totalDiscount.add(dAmt);
        }
        boolean hasDiscount = totalDiscount.compareTo(BigDecimal.ZERO) > 0;

        float totalsX = x + w * 0.55f;
        float lineY = yCursor - 10;

        if (hasDiscount) {
            // Level 2: show Gross, Discount, then Taxable
            drawTotalLine(cb, "Gross Value:", grossTaxable, totalsX, x + w - 6, lineY);
            lineY -= 9;
            drawTotalLine(cb, "Discount:", totalDiscount.negate(), totalsX, x + w - 6, lineY);
            lineY -= 9;
            drawTotalLine(cb, "Taxable Value:", invoice.getSubTotal(), totalsX, x + w - 6, lineY);
            lineY -= 9;
        } else {
            drawTotalLine(cb, "Taxable Value:", invoice.getSubTotal(), totalsX, x + w - 6, lineY);
            lineY -= 9;
        }

        if (invoice.getIgst() != null && invoice.getIgst().compareTo(BigDecimal.ZERO) > 0) {
            drawTotalLine(cb, "IGST:", invoice.getIgst(), totalsX, x + w - 6, lineY);
            lineY -= 9;
        } else {
            drawTotalLine(cb, "CGST:", invoice.getCgst(), totalsX, x + w - 6, lineY);
            lineY -= 9;
            drawTotalLine(cb, "SGST:", invoice.getSgst(), totalsX, x + w - 6, lineY);
            lineY -= 9;
        }
        if (invoice.getRoundOff() != null && invoice.getRoundOff().compareTo(BigDecimal.ZERO) != 0) {
            drawTotalLine(cb, "Round Off:", invoice.getRoundOff(), totalsX, x + w - 6, lineY);
            lineY -= 9;
        }

        // Grand total — bigger
        cb.setLineWidth(0.4f);
        cb.moveTo(totalsX, lineY + 1);
        cb.lineTo(x + w - 6, lineY + 1);
        cb.stroke();
        lineY -= 10;
        drawText(cb, "Grand Total:", totalsX, lineY, F_TOTAL);
        drawText(cb, "Rs. " + money(invoice.getGrandTotal()),
                x + w - 6 - 65, lineY, F_TOTAL);

        // Amount in words (left side under items)
        yCursor -= 4;
        drawText(cb, "Amount in words: " + amountInWords(invoice.getGrandTotal()),
                x + 6, yCursor - 10, F_SMALL);

        // ---- Signature blocks (bottom) ----
        float sigY = y + 8;
        drawText(cb, "Customer Signature", x + 20, sigY, F_SMALL);
        drawText(cb, "For " + safe(company.getName()), x + w - 130, sigY, F_SMALL);
        cb.setLineWidth(0.3f);
        cb.moveTo(x + 15, sigY + 14);
        cb.lineTo(x + 130, sigY + 14);
        cb.stroke();
        cb.moveTo(x + w - 140, sigY + 14);
        cb.lineTo(x + w - 15, sigY + 14);
        cb.stroke();
    }

    // ---------------- helpers ----------------

    private void drawText(PdfContentByte cb, String text, float x, float y, Font font) {
        cb.beginText();
        cb.setFontAndSize(font.getBaseFont(), font.getSize());
        cb.setTextMatrix(x, y);
        cb.showText(text != null ? text : "");
        cb.endText();
    }

    private void drawTotalLine(PdfContentByte cb, String label, BigDecimal value,
                               float leftX, float rightX, float y) {
        drawText(cb, label, leftX, y, F_NORMAL);
        drawText(cb, "Rs. " + money(value), rightX - 55, y, F_NORMAL);
    }

    private String money(BigDecimal v) {
        if (v == null) return "0.00";
        return v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private String stripTrailingZeros(BigDecimal v) {
        if (v == null) return "0";
        return v.stripTrailingZeros().toPlainString();
    }

    private String safe(String s) { return s == null ? "" : s; }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    /** Converts a rupee amount into Indian-format words. */
    private String amountInWords(BigDecimal amount) {
        if (amount == null) return "Zero Rupees Only";
        long rupees = amount.setScale(0, java.math.RoundingMode.HALF_UP).longValue();
        if (rupees == 0) return "Zero Rupees Only";

        String[] ones = { "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
                "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
                "Seventeen", "Eighteen", "Nineteen" };
        String[] tens = { "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety" };

        java.util.function.Function<Integer, String> twoDigit = n -> {
            if (n < 20) return ones[n];
            return tens[n / 10] + (n % 10 == 0 ? "" : " " + ones[n % 10]);
        };

        StringBuilder sb = new StringBuilder();
        long crore = rupees / 10000000; rupees %= 10000000;
        long lakh  = rupees / 100000;   rupees %= 100000;
        long thousand = rupees / 1000;  rupees %= 1000;
        int hundred = (int) (rupees / 100);
        int rest = (int) (rupees % 100);

        if (crore > 0)    sb.append(twoDigit.apply((int) crore)).append(" Crore ");
        if (lakh > 0)     sb.append(twoDigit.apply((int) lakh)).append(" Lakh ");
        if (thousand > 0) sb.append(twoDigit.apply((int) thousand)).append(" Thousand ");
        if (hundred > 0)  sb.append(ones[hundred]).append(" Hundred ");
        if (rest > 0)     sb.append(twoDigit.apply(rest));

        return sb.toString().trim() + " Rupees Only";
    }
}
