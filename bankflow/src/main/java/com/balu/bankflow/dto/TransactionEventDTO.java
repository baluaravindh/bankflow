package com.balu.bankflow.dto;

import com.balu.bankflow.entity.Transaction;
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
public class TransactionEventDTO {

    private String transactionId;
    private Transaction.TransactionType transactionType;
    private BigDecimal amount;
    private String fromAccountNumber;
    private String toAccountNumber;
    private Transaction.TransactionStatus status;
    private LocalDateTime timestamp;
}
