package com.balu.bankflow.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequestDTO {

    @NotBlank(message = "To account number is required. Otherwise it is not possible to transfer amount.")
    private String toAccountNumber;

    @NotNull(message = "Amount is required.")
    @DecimalMin(value = "1.00", message = "Minimum one rupee have to send.")
    private BigDecimal amount;

    private String description;
}
