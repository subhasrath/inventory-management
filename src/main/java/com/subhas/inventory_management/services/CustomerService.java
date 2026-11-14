package com.subhas.inventory_management.services;

import com.subhas.inventory_management.dtos.CustomerDto;
import com.subhas.inventory_management.entity.Customer;
import com.subhas.inventory_management.exception.ApiException;
import com.subhas.inventory_management.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    // ---------------- READ ----------------
    public List<Customer> list(String q) {
        return (q != null && !q.isBlank())
                ? customerRepository.findByFullNameContainingIgnoreCase(q)
                : customerRepository.findAll();
    }

    public Customer get(Integer id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        "Customer not found: " + id, HttpStatus.NOT_FOUND));
    }

    // ---------------- WALK-IN AUTocomplete ----------------
    public List<Customer> walkInSearch(String q) {
        return customerRepository
                .findByFullNameContainingIgnoreCaseAndCustomerTypeAndStatus(
                        q, "WALK_IN", "Active");
    }

    // ---------------- CREATE ----------------
    @Transactional
    public Customer create(CustomerDto dto) {
        if (dto.getFullName() == null || dto.getFullName().isBlank()) {
            throw new ApiException("Customer name is required", HttpStatus.BAD_REQUEST);
        }
        return customerRepository.save(apply(new Customer(), dto));
    }

    // ---------------- UPDATE ----------------
    @Transactional
    public Customer update(Integer id, CustomerDto dto) {
        return customerRepository.save(apply(get(id), dto));
    }

    // ---------------- DEACTIVATE ----------------
    @Transactional
    public void deactivate(Integer id) {
        Customer c = get(id);
        c.setStatus("Inactive");
        customerRepository.save(c);
    }

    // ---------------- Helper: copy DTO → entity + derive type ----------------
    public Customer apply(Customer t, CustomerDto d) {
        if (d.getFullName() != null) t.setFullName(d.getFullName());
        if (d.getGstin() != null)    t.setGstin(d.getGstin());
        if (d.getMobile() != null)   t.setMobile(d.getMobile());
        if (d.getPhone2() != null)   t.setPhone2(d.getPhone2());
        if (d.getEmail() != null)    t.setEmail(d.getEmail());
        if (d.getAddress() != null)  t.setAddress(d.getAddress());
        if (d.getAddress2() != null) t.setAddress2(d.getAddress2());
        if (d.getCity() != null)     t.setCity(d.getCity());
        if (d.getDistrict() != null) t.setDistrict(d.getDistrict());
        if (d.getState() != null)    t.setState(d.getState());
        if (d.getPincode() != null)  t.setPincode(d.getPincode());
        if (d.getStatus() != null)   t.setStatus(d.getStatus());

        // Derive type from GSTIN
        t.setCustomerType(
                (t.getGstin() != null && !t.getGstin().isBlank())
                        ? "REGISTERED" : "WALK_IN"
        );
        return t;
    }

    // ---------------- Helper: entity → DTO ----------------
    public CustomerDto toDto(Customer c) {
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
                .customerType(c.getCustomerType())
                .status(c.getStatus())
                .build();
    }
}
