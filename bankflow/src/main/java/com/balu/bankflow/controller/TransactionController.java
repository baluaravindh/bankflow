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
import org.springframework.web.bind.annotation.*;

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
}
