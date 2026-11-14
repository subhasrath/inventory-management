package com.subhas.inventory_management.dtos;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserDto {
    private Integer userID;
    private String fullName;
    private String username;
    private String password;     // only used on create / change-password
    private String role;         // ADMIN / OPERATOR
    private String status;       // Active / Inactive
}
