package com.balu.bankflow.dto;

import com.balu.bankflow.entity.BankAccount;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAccountStatusRequestDTO {

    @NotNull(message = "Account status is required.")
    private BankAccount.AccountStatus accountStatus;

    @NotBlank(message = "Reason is required to update account status.")
    private String reason;
}
