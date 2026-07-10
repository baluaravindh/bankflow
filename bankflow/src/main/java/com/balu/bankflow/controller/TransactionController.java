package com.balu.bankflow.controller;

import com.balu.bankflow.dto.DepositWithdrawRequestDTO;
import com.balu.bankflow.dto.TransactionResponseDTO;
import com.balu.bankflow.dto.TransferRequestDTO;
import com.balu.bankflow.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction Management APIs")
public class TransactionController {

    private final TransactionService transactionService;

    // POST /api/transactions/transfer/{fromAccountId}
    // Body: TransferRequestDTO (validated with @Valid)
    // Returns: 200 OK with TransactionResponseDTO
    @Operation(summary = "Transfer")
    @PostMapping("/transfer/{fromAccountId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TransactionResponseDTO> transfer(
            @PathVariable Long fromAccountId,
            @Valid @RequestBody TransferRequestDTO dto) {
        return ResponseEntity.ok(transactionService.transfer(fromAccountId, dto));
    }

    // POST /api/transactions/deposit/{accountNumber}
    // Body: DepositWithdrawRequestDTO (validated with @Valid)
    // Returns: 200 OK with TransactionResponseDTO
    @Operation(summary = "Deposit")
    @PostMapping("/deposit/{accountNumber}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TransactionResponseDTO> deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody DepositWithdrawRequestDTO dto) {
        return ResponseEntity.ok(transactionService.deposit(accountNumber, dto));
    }

    // POST /api/transactions/withdraw/{accountNumber}
    // Body: DepositWithdrawRequestDTO (validated with @Valid)
    // Returns: 200 OK with TransactionResponseDTO
    @Operation(summary = "Withdraw")
    @PostMapping("/withdraw/{accountNumber}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TransactionResponseDTO> withdraw(
            @PathVariable String accountNumber,
            @Valid @RequestBody DepositWithdrawRequestDTO dto) {
        return ResponseEntity.ok(transactionService.withdraw(accountNumber, dto));
    }

    // GET /api/transactions/history/{accountNumber}
    // Access: CUSTOMER or BANK_ADMIN
    // Note: get email from SecurityContext
    @Operation(summary = "Get Transaction History")
    @GetMapping("/history/{accountNumber}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('BANK_ADMIN')")
    public ResponseEntity<List<TransactionResponseDTO>>
    getTransactionHistory(@PathVariable String accountNumber) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(transactionService.getTransactionHistory(accountNumber, email));
    }

    // GET /api/transactions/history/{accountNumber}/type
    // Access: CUSTOMER or BANK_ADMIN
    // Request param: transactionType (String)
    @Operation(summary = "Get Transactions By Type")
    @GetMapping("/history/{accountNumber}/type")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('BANK_ADMIN')")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByType(@PathVariable String accountNumber,
                                                                              @RequestParam String transactionType) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(transactionService.getTransactionsByType(accountNumber, transactionType, email));
    }

    // GET /api/transactions/history/{accountNumber}/date-range
    // Access: CUSTOMER or BANK_ADMIN
    // Request params: from (LocalDateTime), to (LocalDateTime)
    @Operation(summary = "Get Transactions By Date Range")
    @GetMapping("/history/{accountNumber}/date-range")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('BANK_ADMIN')")
    public ResponseEntity<List<TransactionResponseDTO>>
    getTransactionsByDateRange(@PathVariable String accountNumber,
                               @RequestParam LocalDateTime start,
                               @RequestParam LocalDateTime end) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(transactionService.getTransactionsByDateRange(accountNumber, start, end, email));
    }

    // GET /api/transactions/{transactionId}
    // Access: CUSTOMER or BANK_ADMIN
    // Note: get email from SecurityContext
    @Operation(summary = "Get Transaction By Id")
    @GetMapping("/{transactionId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('BANK_ADMIN')")
    public ResponseEntity<TransactionResponseDTO> getTransactionById(@PathVariable String transactionId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId, email));
    }
}
