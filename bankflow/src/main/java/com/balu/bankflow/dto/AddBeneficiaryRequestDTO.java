package com.balu.bankflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddBeneficiaryRequestDTO {

    // beneficiaryName - @NotBlank
    @NotBlank(message = "Beneficiary name is required.")
    private String beneficiaryName;

    // accountNumber - @NotBlank
    @NotBlank(message = "Account number is required.")
    private String accountNumber;

    // bankName - @NotBlank
    @NotBlank(message = "Bank name is required.")
    private String bankName;

    // ifscCode - @NotBlank
    @NotBlank(message = "IFSC Code is required.")
    private String ifscCode;
}
