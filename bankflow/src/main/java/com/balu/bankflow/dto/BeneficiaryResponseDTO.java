package com.balu.bankflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BeneficiaryResponseDTO {

    private Long id;
    private String beneficiaryName;
    private String accountNumber;
    private String bankName;
    private String ifscCode;
    private LocalDateTime createdAt;
}
