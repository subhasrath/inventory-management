package com.subhas.inventory_management.dtos;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginResponse {
    private boolean success;
    private String message;
    private Integer userID;
    private String fullName;
    private String username;
    private String role;
    private String token;
}
