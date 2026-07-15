package com.balu.bankflow.repository;

import com.balu.bankflow.entity.BankAccount;
import com.balu.bankflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    // Find all accounts by user
    List<BankAccount> findAllByUser(User user);

    BankAccount findByUser(User user);

    // Find account by account number
    Optional<BankAccount> findByAccountNumber(String accountNumber);

    // Find all accounts by user and status
    List<BankAccount> findAllByUser_IdAndStatus(Long userId, BankAccount.AccountStatus status);

    // Check if account number exists (boolean)
    boolean existsByAccountNumber(String accountNumber);

    // Find all accounts by status (for admin)
    List<BankAccount> findAllByStatus(BankAccount.AccountStatus status);
}
