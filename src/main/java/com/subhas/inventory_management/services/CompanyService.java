package com.subhas.inventory_management.services;

import com.subhas.inventory_management.dtos.CompanyDto;
import com.subhas.inventory_management.entity.Company;
import com.subhas.inventory_management.exception.ApiException;
import com.subhas.inventory_management.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    /** Returns the single company row, or null if not configured yet. */
    public CompanyDto get() {
        return companyRepository.findAll().stream().findFirst()
                .map(this::toDto).orElse(null);
    }

    /** True once shop details have been filled in. */
    public boolean isConfigured() {
        return companyRepository.count() > 0;
    }

    /** Create OR update the single company row. */
    @Transactional
    public CompanyDto save(CompanyDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()
                || dto.getGstin() == null || dto.getGstin().isBlank()
                || dto.getState() == null || dto.getState().isBlank()) {
            throw new ApiException("Name, GSTIN, and state are required",
                    HttpStatus.BAD_REQUEST);
        }

        Company c = companyRepository.findAll().stream().findFirst()
                .orElseGet(Company::new);

        c.setName(dto.getName());
        c.setGstin(dto.getGstin());
        c.setAddress(dto.getAddress() != null ? dto.getAddress() : "");
        c.setCity(dto.getCity() != null ? dto.getCity() : "");
        c.setState(dto.getState());
        c.setPincode(dto.getPincode());
        c.setPhone(dto.getPhone());
        c.setEmail(dto.getEmail());
        c.setLogoPath(dto.getLogoPath());
        if (dto.getInvoicePrefix() != null && !dto.getInvoicePrefix().isBlank()) {
            c.setInvoicePrefix(dto.getInvoicePrefix());
        } else if (c.getInvoicePrefix() == null) {
            c.setInvoicePrefix("INV");
        }
        if (dto.getNextInvoiceNo() != null) {
            c.setNextInvoiceNo(dto.getNextInvoiceNo());
        } else if (c.getNextInvoiceNo() == null) {
            c.setNextInvoiceNo(1);
        }

        return toDto(companyRepository.save(c));
    }

    private CompanyDto toDto(Company c) {
        return CompanyDto.builder()
                .companyID(c.getCompanyID())
                .name(c.getName())
                .gstin(c.getGstin())
                .address(c.getAddress())
                .city(c.getCity())
                .state(c.getState())
                .pincode(c.getPincode())
                .phone(c.getPhone())
                .email(c.getEmail())
                .logoPath(c.getLogoPath())
                .invoicePrefix(c.getInvoicePrefix())
                .nextInvoiceNo(c.getNextInvoiceNo())
                .build();
    }
}
