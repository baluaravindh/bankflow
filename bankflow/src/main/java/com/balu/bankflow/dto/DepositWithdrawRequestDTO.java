package com.balu.bankflow.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositWithdrawRequestDTO {

    @NotBlank(message = "Account number is required for withdrawn amount.")
    private String accountNumber;

    @NotNull(message = "Amount is required.")
    @DecimalMin(value = "1.00", message = "Amount should atleast one rupee to enter to withdrawn.")
    private BigDecimal amount;

    private String description;
}
