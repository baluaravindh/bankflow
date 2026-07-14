package com.balu.bankflow.controller;

import com.balu.bankflow.dto.AddBeneficiaryRequestDTO;
import com.balu.bankflow.dto.BeneficiaryResponseDTO;
import com.balu.bankflow.service.BeneficiaryService;
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
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
@Tag(name = "Beneficiary", description = "Beneficiary Management APIs")
@Slf4j
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    // POST /api/beneficiaries
    // Access: CUSTOMER only
    // Request: @Valid @RequestBody AddBeneficiaryRequestDTO
    // Response: 201 + BeneficiaryResponseDTO
    // Note: get email from SecurityContext
    @Operation(summary = "Add Beneficiary")
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BeneficiaryResponseDTO> addBeneficiary(@Valid @RequestBody AddBeneficiaryRequestDTO dto) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(beneficiaryService.addBeneficiary(email, dto));
    }

    // GET /api/beneficiaries
    // Access: CUSTOMER only
    // Response: 200 + List<BeneficiaryResponseDTO>
    // Note: get email from SecurityContext
    @Operation(summary = "Get My Beneficiaries")
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<BeneficiaryResponseDTO>> getMyBeneficiaries() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        return ResponseEntity.ok(beneficiaryService.getMyBeneficiaries(email));
    }

    // DELETE /api/beneficiaries/{beneficiaryId}
    // Access: CUSTOMER only
    // Response: 200 + String
    // Note: get email from SecurityContext
    @Operation(summary = "Remove Beneficiary")
    @DeleteMapping("/{beneficiaryId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> removeBeneficiary(@PathVariable Long beneficiaryId) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        beneficiaryService.removeBeneficiary(beneficiaryId, email);
        return ResponseEntity.ok("Beneficiary has been removed successfully");
    }
}
