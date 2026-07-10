package com.balu.bankflow.repository;

import com.balu.bankflow.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    // Find by transactionId
    Optional<Transaction> findByTransactionId(String transactionId);

    // Find all transactions by fromAccount
    List<Transaction> findAllByFromAccountId(Long fromAccountId);

    // Find all transactions by toAccount
    List<Transaction> findAllByToAccountId(Long toAccountId);

    // Find all transactions by fromAccount or toAccount
    // (all transactions for an account — both sent and received)
    List<Transaction> findAllByFromAccountIdOrToAccountId(Long fromAccountId, Long toAccountId);

    // Find all transactions by fromAccount
    //   and createdAt between two dates
    // (for daily limit check)
    List<Transaction> findAllByFromAccountIdAndCreatedAtBetween(
            Long fromAccountId,
            LocalDateTime start,
            LocalDateTime end);

    // Count transactions by fromAccount
    //   and createdAt after a given time
    // (for fraud detection — more than 5 in 1 hour)
    int countByFromAccountIdAndCreatedAtAfter(Long fromAccountId, LocalDateTime after);
}
