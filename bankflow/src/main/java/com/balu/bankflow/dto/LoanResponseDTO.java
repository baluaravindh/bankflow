package com.balu.bankflow.dto;

import com.balu.bankflow.entity.Loan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanResponseDTO {

    private Long id;
    private Loan.LoanType loanType;
    private BigDecimal loanAmount;
    private Integer tenure;
    private BigDecimal interestRate;
    private BigDecimal emiAmount;
    private Loan.LoanStatus status;
    private String remarks;
    private LocalDateTime createdAt;
}
