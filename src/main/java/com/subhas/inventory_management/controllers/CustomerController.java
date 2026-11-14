package com.subhas.inventory_management.controllers;

import com.subhas.inventory_management.dtos.CustomerDto;
import com.subhas.inventory_management.entity.Customer;
import com.subhas.inventory_management.exception.ApiException;
import com.subhas.inventory_management.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository repo;

    @GetMapping
    public List<CustomerDto> list(@RequestParam(required = false) String q) {
        List<Customer> list = (q != null && !q.isBlank())
                ? repo.findByFullNameContainingIgnoreCase(q)
                : repo.findAll();
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public CustomerDto get(@PathVariable Integer id) {
        return toDto(find(id));
    }

    @PostMapping
    public ResponseEntity<CustomerDto> create(@RequestBody CustomerDto dto) {
        Customer saved = repo.save(apply(new Customer(), dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public CustomerDto update(@PathVariable Integer id, @RequestBody CustomerDto dto) {
        return toDto(repo.save(apply(find(id), dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Integer id) {
        Customer c = find(id);
        c.setStatus("Inactive");
        repo.save(c);
        return ResponseEntity.noContent().build();
    }

    private Customer find(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new ApiException("Customer not found: " + id, HttpStatus.NOT_FOUND));
    }

    private Customer apply(Customer t, CustomerDto d) {
        t.setFullName(d.getFullName());
        t.setGstin(d.getGstin());
        t.setMobile(d.getMobile());
        t.setPhone2(d.getPhone2());
        t.setEmail(d.getEmail());
        t.setAddress(d.getAddress());
        t.setAddress2(d.getAddress2());
        t.setCity(d.getCity());
        t.setDistrict(d.getDistrict());
        t.setState(d.getState());
        t.setPincode(d.getPincode());
        if (d.getStatus() != null) t.setStatus(d.getStatus());
        return t;
    }

    private CustomerDto toDto(Customer c) {
        return CustomerDto.builder()
                .customerID(c.getCustomerID())
                .fullName(c.getFullName())
                .gstin(c.getGstin())
                .mobile(c.getMobile())
                .phone2(c.getPhone2())
                .email(c.getEmail())
                .address(c.getAddress())
                .address2(c.getAddress2())
                .city(c.getCity())
                .district(c.getDistrict())
                .state(c.getState())
                .pincode(c.getPincode())
                .status(c.getStatus())
                .build();
    }
}
