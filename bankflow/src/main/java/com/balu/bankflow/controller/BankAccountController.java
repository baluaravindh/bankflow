package com.balu.bankflow.controller;

import com.balu.bankflow.dto.AccountResponseDTO;
import com.balu.bankflow.dto.CreateAccountRequestDTO;
import com.balu.bankflow.dto.UpdateAccountStatusRequestDTO;
import com.balu.bankflow.service.BankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Bank Account", description = "Bank Account Management APIs")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    // POST /api/accounts/create
    // Access: CUSTOMER only
    // Request: @Valid @RequestBody CreateAccountRequestDTO
    // Response: 201 + AccountResponseDTO
    // Note: get email from SecurityContext
    @Operation(summary = "Create Account")
    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<AccountResponseDTO> createAccount(
            @Valid @RequestBody CreateAccountRequestDTO dto) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(bankAccountService.createAccount(email, dto));
    }

    // GET /api/accounts/my-accounts
    // Access: CUSTOMER only
    // Response: 200 + List<AccountResponseDTO>
    // Note: get email from SecurityContext
    @Operation(summary = "Get My Accounts")
    @GetMapping("/my-accounts")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<AccountResponseDTO>> getMyAccounts() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        return ResponseEntity.ok(bankAccountService.getMyAccounts(email));
    }

    // GET /api/accounts/{accountNumber}
    // Access: CUSTOMER or BANK_ADMIN
    // Response: 200 + AccountResponseDTO
    // Note: get email from SecurityContext
    @Operation(summary = "Get Account By Number")
    @GetMapping("/{accountNumber}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('BANK_ADMIN')")
    public ResponseEntity<AccountResponseDTO> getAccountByNumber(@PathVariable String accountNumber) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return ResponseEntity.ok(bankAccountService.getAccountByNumber(accountNumber, email));
    }

    // GET /api/accounts/all
    // Access: BANK_ADMIN only
    // Response: 200 + List<AccountResponseDTO>
    @Operation(summary = "Get All Accounts")
    @GetMapping("/all")
    @PreAuthorize("hasRole('BANK_ADMIN')")
    public ResponseEntity<List<AccountResponseDTO>> getAllAccounts() {
        return ResponseEntity.ok(bankAccountService.getAllAccounts());
    }

    // PUT /api/accounts/{accountNumber}/status
    // Access: BANK_ADMIN only
    // Request: @Valid @RequestBody UpdateAccountStatusRequestDTO
    // Response: 200 + AccountResponseDTO
    @Operation(summary = "Update Account Status")
    @PutMapping("/{accountNumber}/status")
    @PreAuthorize("hasRole('BANK_ADMIN')")
    public ResponseEntity<AccountResponseDTO> updateAccountStatus(
            @PathVariable String accountNumber,
            @Valid @RequestBody UpdateAccountStatusRequestDTO dto) {
        return ResponseEntity.ok(bankAccountService.updateAccountStatus(accountNumber, dto));
    }
}
