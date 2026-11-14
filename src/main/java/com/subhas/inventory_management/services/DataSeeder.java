package com.subhas.inventory_management.services;

import com.subhas.inventory_management.entity.Company;
import com.subhas.inventory_management.entity.User;
import com.subhas.inventory_management.repository.CompanyRepository;
import com.subhas.inventory_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Bean
    CommandLineRunner seed() {
        return args -> {
            if (companyRepository.count() == 0) {
                companyRepository.save(Company.builder()
                        .name("Your Distributor Shop")
                        .gstin("27ABCDE1234F1Z5")
                        .address("123 Main Road")
                        .city("Mumbai")
                        .state("Maharashtra")
                        .pincode("400001")
                        .phone("9999999999")
                        .email("shop@example.com")
                        .invoicePrefix("INV")
                        .nextInvoiceNo(1)
                        .build());
            }
            if (userRepository.count() == 0) {
                userRepository.save(User.builder()
                        .fullName("Administrator")
                        .username("admin")
                        .password("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy")
                        .role("ADMIN").status("Active").build());
            }
        };
    }
}
