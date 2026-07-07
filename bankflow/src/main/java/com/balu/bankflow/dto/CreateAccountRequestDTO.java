package com.balu.bankflow.dto;

import com.balu.bankflow.entity.BankAccount;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAccountRequestDTO {

    @NotNull(message = "Account type is required to create account.")
    private BankAccount.AccountType accountType;
}
