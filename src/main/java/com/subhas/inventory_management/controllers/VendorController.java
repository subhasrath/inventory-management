package com.subhas.inventory_management.controllers;

import com.subhas.inventory_management.dtos.VendorDto;
import com.subhas.inventory_management.entity.Vendor;
import com.subhas.inventory_management.exception.ApiException;
import com.subhas.inventory_management.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorRepository repo;

    @GetMapping
    public List<VendorDto> list() {
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public VendorDto get(@PathVariable Integer id) {
        return toDto(find(id));
    }

    @PostMapping
    public ResponseEntity<VendorDto> create(@RequestBody VendorDto dto) {
        Vendor saved = repo.save(apply(new Vendor(), dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public VendorDto update(@PathVariable Integer id, @RequestBody VendorDto dto) {
        return toDto(repo.save(apply(find(id), dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Integer id) {
        Vendor v = find(id);
        v.setStatus("Inactive");
        repo.save(v);
        return ResponseEntity.noContent().build();
    }

    private Vendor find(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new ApiException("Vendor not found: " + id, HttpStatus.NOT_FOUND));
    }

    private Vendor apply(Vendor t, VendorDto d) {
        t.setFullName(d.getFullName());
        t.setGstin(d.getGstin());
        t.setEmail(d.getEmail());
        t.setMobile(d.getMobile());
        t.setPhone2(d.getPhone2());
        t.setAddress(d.getAddress());
        t.setAddress2(d.getAddress2());
        t.setCity(d.getCity());
        t.setDistrict(d.getDistrict());
        t.setState(d.getState());
        t.setPincode(d.getPincode());
        if (d.getStatus() != null) t.setStatus(d.getStatus());
        return t;
    }

    private VendorDto toDto(Vendor v) {
        return VendorDto.builder()
                .vendorID(v.getVendorID())
                .fullName(v.getFullName())
                .gstin(v.getGstin())
                .email(v.getEmail())
                .mobile(v.getMobile())
                .phone2(v.getPhone2())
                .address(v.getAddress())
                .address2(v.getAddress2())
                .city(v.getCity())
                .district(v.getDistrict())
                .state(v.getState())
                .pincode(v.getPincode())
                .status(v.getStatus())
                .build();
    }
}
