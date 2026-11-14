package com.subhas.inventory_management.dtos;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SignupRequest {
    private String fullName;
    private String username;
    private String password;
    private String confirmPassword;
}
