package com.subhas.inventory_management.controllers;

import com.subhas.inventory_management.dtos.CompanyDto;
import com.subhas.inventory_management.services.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    /** Null if not configured yet — Angular uses this to gate the UI. */
    @GetMapping
    public ResponseEntity<CompanyDto> get() {
        return ResponseEntity.ok(companyService.get());
    }

    /** Simple boolean for the onboarding check. */
    @GetMapping("/configured")
    public Map<String, Boolean> configured() {
        return Map.of("configured", companyService.isConfigured());
    }

    /** POST and PUT both upsert (create-or-update the single row). */
    @PostMapping
    public CompanyDto save(@RequestBody CompanyDto dto) {
        return companyService.save(dto);
    }

    @PutMapping
    public CompanyDto update(@RequestBody CompanyDto dto) {
        return companyService.save(dto);
    }
}
