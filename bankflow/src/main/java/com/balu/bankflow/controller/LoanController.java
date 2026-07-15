package com.balu.bankflow.controller;

import com.balu.bankflow.dto.LoanApplicationRequestDTO;
import com.balu.bankflow.dto.LoanResponseDTO;
import com.balu.bankflow.dto.LoanStatusUpdateRequestDTO;
import com.balu.bankflow.entity.Loan;
import com.balu.bankflow.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Loan Management APIs")
@Slf4j
public class LoanController {

    private final LoanService loanService;

    //    POST /api/loans — CUSTOMER applies for a loan
    @Operation(summary = "Apply For Loan")
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<LoanResponseDTO> applyForLoan(
            @Valid @RequestBody LoanApplicationRequestDTO dto) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.applyForLoan(email, dto));
    }
    //    GET /api/loans/my — CUSTOMER views their own loans
    @Operation(summary = "Get My Loans")
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<LoanResponseDTO>> getMyLoans(){
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        return ResponseEntity.ok(loanService.getMyLoans(email));
    }

    //    GET /api/loans/status/{status} — ADMIN views loans by status
    @Operation(summary = "Get Loans By Status")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('BANK_ADMIN')")
    public ResponseEntity<List<LoanResponseDTO>> getLoansByStatus(@PathVariable Loan.LoanStatus status) {
        return ResponseEntity.ok(loanService.getLoansByStatus(status));
    }

    //    PUT /api/loans/{loanId}/status — ADMIN approves/rejects a loan
    @Operation(summary = "Update Loan Status")
    @PutMapping("/{loanId}/status")
    @PreAuthorize("hasRole('BANK_ADMIN')")
    public ResponseEntity<LoanResponseDTO> updateLoanStatus(@PathVariable Long loanId,
                                                            @Valid @RequestBody LoanStatusUpdateRequestDTO dto) {
        return ResponseEntity.ok(loanService.updateLoanStatus(loanId, dto));
    }
}
