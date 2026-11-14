package com.subhas.inventory_management.controllers;

import com.subhas.inventory_management.entity.Invoice;
import com.subhas.inventory_management.exception.ApiException;
import com.subhas.inventory_management.repository.InvoiceRepository;
import com.subhas.inventory_management.services.PdfInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class PdfController {

    private final PdfInvoiceService pdfInvoiceService;
    private final InvoiceRepository invoiceRepository;

    /** Download the 2-per-A4 GST invoice PDF. */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Integer id) {
        Invoice inv = invoiceRepository.findById(id)
                .orElseThrow(() -> new ApiException("Invoice not found: " + id, HttpStatus.NOT_FOUND));

        byte[] pdf = pdfInvoiceService.generate(id);

        String filename = (inv.getInvoiceNo() != null ? inv.getInvoiceNo() : "invoice-" + id) + ".pdf";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdf.length);
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    /** Preview in browser (inline) instead of download. */
    @GetMapping("/{id}/pdf/preview")
    public ResponseEntity<byte[]> previewPdf(@PathVariable Integer id) {
        Invoice inv = invoiceRepository.findById(id)
                .orElseThrow(() -> new ApiException("Invoice not found: " + id, HttpStatus.NOT_FOUND));

        byte[] pdf = pdfInvoiceService.generate(id);

        String filename = (inv.getInvoiceNo() != null ? inv.getInvoiceNo() : "invoice-" + id) + ".pdf";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");
        headers.setContentLength(pdf.length);

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
