package com.balu.bankflow.dto;

import com.balu.bankflow.entity.Loan;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanStatusUpdateRequestDTO {

    //   - status (LoanStatus enum) - not null, should only allow APPROVED/REJECTED (validate in service)
    @NotNull(message = "Loan status is required.")
    private Loan.LoanStatus status;

    //   - remarks (String) - nullable, admin's comment
    private String remarks;
}
