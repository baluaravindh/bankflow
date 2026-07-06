package com.balu.bankflow.controller;

import com.balu.bankflow.dto.*;
import com.balu.bankflow.security.JwtUtil;
import com.balu.bankflow.service.RefreshTokenService;
import com.balu.bankflow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, Login, Logout APIs")
@Slf4j
public class AuthController {

    private final UserService userService;

    // POST /api/auth/register → register()
    // Access: public
    // Request: @Valid @RequestBody RegisterRequestDTO
    // Response: 201 + AuthResponseDTO
    @Operation(summary = "Register")
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(dto));
    }

    // POST /api/auth/login → login()
    // Access: public
    // Request: @Valid @RequestBody LoginRequestDTO
    // Response: 200 + AuthResponseDTO
    @Operation(summary = "Login")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(userService.login(dto));
    }

    // POST /api/auth/refresh-token → refreshToken()
    // Access: public
    // Request: @Valid @RequestBody RefreshTokenRequestDTO
    // Response: 200 + AuthResponseDTO
    @Operation(summary = "Refresh Token")
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDTO> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO dto) {
        return ResponseEntity.ok(userService.refreshToken(dto.getRefreshToken()));
    }

    // POST /api/auth/logout → logout()
    // Access: authenticated
    // Request: none (get email from SecurityContext)
    // Response: 200 + String
    @Operation(summary = "Logout")
    @PostMapping("/logout")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('BANK_ADMIN')")
    public ResponseEntity<String> logout() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        userService.logout(email);
        return ResponseEntity.ok("User logged out successfully.");
    }

    // POST /api/auth/change-password → changePassword()
    // Access: authenticated
    // Request: @Valid @RequestBody ChangePasswordRequestDTO
    // Response: 200 + String
    @Operation(summary = "Change Password")
    @PostMapping("/change-password")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('BANK_ADMIN')")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO dto) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        userService.changePassword(email, dto);
        return ResponseEntity.ok("Password changed successfully.");
    }
}
