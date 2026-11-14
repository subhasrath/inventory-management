package com.subhas.inventory_management.controllers;

import com.subhas.inventory_management.dtos.CustomerDto;
import com.subhas.inventory_management.dtos.InvoiceDto;
import com.subhas.inventory_management.entity.Customer;
import com.subhas.inventory_management.repository.InvoiceRepository;
import com.subhas.inventory_management.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceController invoiceController;   // for toDto reuse

    // ---------------- LIST / SEARCH ----------------
    @GetMapping
    public List<CustomerDto> list(@RequestParam(required = false) String q) {
        return customerService.list(q).stream()
                .map(customerService::toDto)
                .toList();
    }

    // ---------------- GET ONE ----------------
    @GetMapping("/{id}")
    public CustomerDto get(@PathVariable Integer id) {
        return customerService.toDto(customerService.get(id));
    }

    // ---------------- WALK-IN autocomplete ----------------
    @GetMapping("/walkin-search")
    public List<CustomerDto> walkInSearch(@RequestParam String q) {
        return customerService.walkInSearch(q).stream()
                .map(customerService::toDto)
                .toList();
    }

    // ---------------- PURCHASE HISTORY ----------------
    @GetMapping("/{id}/invoices")
    @Transactional(readOnly = true)
    public List<InvoiceDto> history(@PathVariable Integer id) {
        return invoiceRepository
                .findByCustomerCustomerIDOrderByInvoiceDateDesc(id)
                .stream()
                .map(i -> invoiceController.toDto(i, false))
                .toList();
    }

    // ---------------- CREATE ----------------
    @PostMapping
    public ResponseEntity<CustomerDto> create(@RequestBody CustomerDto dto) {
        Customer saved = customerService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerService.toDto(saved));
    }

    // ---------------- UPDATE ----------------
    @PutMapping("/{id}")
    public CustomerDto update(@PathVariable Integer id, @RequestBody CustomerDto dto) {
        return customerService.toDto(customerService.update(id, dto));
    }

    // ---------------- DEACTIVATE ----------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Integer id) {
        customerService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
