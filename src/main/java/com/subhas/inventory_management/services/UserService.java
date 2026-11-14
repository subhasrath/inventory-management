package com.subhas.inventory_management.services;

import com.subhas.inventory_management.dtos.LoginRequest;
import com.subhas.inventory_management.dtos.LoginResponse;
import com.subhas.inventory_management.dtos.SignupRequest;
import com.subhas.inventory_management.dtos.UserDto;
import com.subhas.inventory_management.entity.Company;
import com.subhas.inventory_management.entity.User;
import com.subhas.inventory_management.exception.ApiException;
import com.subhas.inventory_management.repository.CompanyRepository;
import com.subhas.inventory_management.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JwtService jwtService;
    private static final java.util.Set<String> VALID_ROLES =
            java.util.Set.of("ADMIN", "OPERATOR");

    private String validateRole(String role) {
        if (role == null || role.isBlank()) return "OPERATOR";
        String r = role.trim().toUpperCase();
        if (!VALID_ROLES.contains(r)) {
            throw new ApiException("Invalid role: " + role + " (allowed: ADMIN, OPERATOR)",
                    HttpStatus.BAD_REQUEST);
        }
        return r;
    }

    // ---------------- LOGIN ----------------
    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElse(null);

        if (user == null) {
            return LoginResponse.builder()
                    .success(false).message("Invalid username or password").build();
        }
        if (!"Active".equalsIgnoreCase(user.getStatus())) {
            return LoginResponse.builder()
                    .success(false).message("Account is inactive").build();
        }
        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            return LoginResponse.builder()
                    .success(false).message("Invalid username or password").build();
        }
        String token = jwtService.generateToken(user);
        return LoginResponse.builder()
                .success(true)
                .message("Login successful")
                .userID(user.getUserID())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .token(token)
                .role(user.getRole())
                .build();
    }

    // ---------------- CRUD ----------------
    public List<User> listAll() {
        return userRepository.findAll();
    }

    public User get(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException("User not found: " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public User create(UserDto dto) {
        // 1. required fields
        if (dto.getFullName() == null || dto.getFullName().isBlank()) {
            throw new ApiException("Full name is required", HttpStatus.BAD_REQUEST);
        }
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new ApiException("Username is required", HttpStatus.BAD_REQUEST);
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 4) {
            throw new ApiException("Password must be at least 4 characters",
                    HttpStatus.BAD_REQUEST);
        }

        // 2. normalize
        String username = dto.getUsername().trim().toLowerCase();
        String role     = validateRole(dto.getRole());
        String status   = dto.getStatus() == null ? "Active" : dto.getStatus().trim();

        // 3. uniqueness
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ApiException("Username already exists: " + username,
                    HttpStatus.CONFLICT);
        }

        // 4. save
        User user = User.builder()
                .fullName(dto.getFullName().trim())
                .username(username)
                .password(encoder.encode(dto.getPassword()))
                .role(role)
                .status(status)
                .build();
        return userRepository.save(user);
    }

    public User update(Integer id, UserDto dto) {
        User user = get(id);
        user.setFullName(dto.getFullName());
        if (dto.getRole() != null)   user.setRole(dto.getRole());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());

        // username change: check uniqueness
        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
                throw new ApiException("Username already exists: " + dto.getUsername(),
                        HttpStatus.CONFLICT);
            }
            user.setUsername(dto.getUsername());
        }
        // password: only if provided
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(encoder.encode(dto.getPassword()));
        }
        return userRepository.save(user);
    }

    public void changePassword(Integer id, String newPassword) {
        if (newPassword == null || newPassword.length() < 4) {
            throw new ApiException("Password must be at least 4 characters",
                    HttpStatus.BAD_REQUEST);
        }
        User user = get(id);
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
    }

    public void deactivate(Integer id) {
        User user = get(id);
        user.setStatus("Inactive");
        userRepository.save(user);
    }

    @Transactional
    public LoginResponse signup(SignupRequest req) {
        // 1. validation
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            throw new ApiException("Username is required", HttpStatus.BAD_REQUEST);
        }
        if (req.getPassword() == null || req.getPassword().length() < 4) {
            throw new ApiException("Password must be at least 4 characters", HttpStatus.BAD_REQUEST);
        }
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new ApiException("Passwords do not match", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new ApiException("Username already taken: " + req.getUsername(),
                    HttpStatus.CONFLICT);
        }

        // 2. Only ONE public signup allowed — the admin.
        if (userRepository.count() > 0) {
            throw new ApiException(
                    "Signup is disabled. Ask an administrator to create your account.",
                    HttpStatus.FORBIDDEN);
        }

        // 3. create the admin
        User user = User.builder()
                .fullName(req.getFullName() != null ? req.getFullName() : req.getUsername())
                .username(req.getUsername())
                .password(encoder.encode(req.getPassword()))
                .role("ADMIN")
                .status("Active")
                .build();
        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);
        return LoginResponse.builder()
                .success(true)
                .message("Admin account created")
                .userID(saved.getUserID())
                .fullName(saved.getFullName())
                .username(saved.getUsername())
                .token(token)
                .role(saved.getRole())
                .build();
    }
}