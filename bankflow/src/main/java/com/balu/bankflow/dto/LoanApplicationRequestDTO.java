package com.balu.bankflow.dto;

import com.balu.bankflow.entity.Loan;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanApplicationRequestDTO {

    //   - loanType (LoanType enum) - not null
    @NotNull(message = "Loan type is required.")
    private Loan.LoanType loanType;

    //   - loanAmount (BigDecimal) - not null, @Positive or @DecimalMin("0.01")
    @NotNull(message = "Loan amount is required.")
    @Positive(message = "It should be positive")
    @DecimalMin("0.01")
    private BigDecimal loanAmount;

    //   - tenure (Integer, months) - not null, @Positive
    @NotNull(message = "Tenure is required.")
    @Positive
    private Integer tenure;

    //   - interestRate (BigDecimal) - not null, @Positive
    @NotNull(message = "Interest rate is required.")
    @Positive
    private BigDecimal interestRate;
}
