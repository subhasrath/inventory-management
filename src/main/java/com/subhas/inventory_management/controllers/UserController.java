package com.subhas.inventory_management.controllers;

import com.subhas.inventory_management.dtos.LoginRequest;
import com.subhas.inventory_management.dtos.LoginResponse;
import com.subhas.inventory_management.dtos.SignupRequest;
import com.subhas.inventory_management.dtos.UserDto;
import com.subhas.inventory_management.entity.User;
import com.subhas.inventory_management.exception.ApiException;
import com.subhas.inventory_management.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ---------------- SIGNUP (first admin only, public) ----------------
    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signup(@RequestBody SignupRequest req) {
        try {
            LoginResponse res = userService.signup(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (ApiException ex) {
            return ResponseEntity.status(ex.getStatus())
                    .body(LoginResponse.builder()
                            .success(false)
                            .message(ex.getMessage())
                            .build());
        }
    }

    // ---------------- LOGIN (public) ----------------
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        LoginResponse res = userService.login(req);
        return res.isSuccess()
                ? ResponseEntity.ok(res)
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    // ---------------- LIST ----------------
    @GetMapping
    public List<UserDto> list() {
        return userService.listAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ---------------- GET ONE ----------------
    @GetMapping("/{id}")
    public UserDto get(@PathVariable Integer id) {
        return toDto(userService.get(id));
    }

    // ---------------- CREATE ----------------
    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody UserDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toDto(userService.create(dto)));
    }

    // ---------------- UPDATE ----------------
    @PutMapping("/{id}")
    public UserDto update(@PathVariable Integer id, @RequestBody UserDto dto) {
        return toDto(userService.update(id, dto));
    }

    // ---------------- CHANGE PASSWORD ----------------
    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Integer id,
                                               @RequestBody Map<String, String> body) {
        userService.changePassword(id, body.get("newPassword"));
        return ResponseEntity.noContent().build();
    }

    // ---------------- DEACTIVATE ----------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Integer id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------- Helper ----------------
    private UserDto toDto(User u) {
        return UserDto.builder()
                .userID(u.getUserID())
                .fullName(u.getFullName())
                .username(u.getUsername())
                // do NOT expose password hash
                .role(u.getRole())
                .status(u.getStatus())
                .build();
    }
}
