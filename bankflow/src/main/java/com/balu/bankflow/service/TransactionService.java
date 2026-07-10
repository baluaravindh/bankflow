package com.balu.bankflow.service;

import com.balu.bankflow.dto.DepositWithdrawRequestDTO;
import com.balu.bankflow.dto.TransactionResponseDTO;
import com.balu.bankflow.dto.TransferRequestDTO;
import com.balu.bankflow.entity.BankAccount;
import com.balu.bankflow.entity.Transaction;
import com.balu.bankflow.exception.*;
import com.balu.bankflow.repository.BankAccountRepository;
import com.balu.bankflow.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;

    //    Business rules for transfer():
    @Transactional
    public TransactionResponseDTO transfer(Long fromAccountId, TransferRequestDTO dto) {

        //    Fetch fromAccount by fromAccountId — throw ResourceNotFoundException if not found.
        BankAccount fromAccount = bankAccountRepository.findById(fromAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("From account not found"));

        String currentUserEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        if (!fromAccount.getUser().getEmail().equals(currentUserEmail)) {
            throw new UnauthorizedAccountAccessException("You do not have access to this account.");
        }

        if (fromAccount.getStatus() != BankAccount.AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Your account is not active.");
        }

        //    Fetch toAccount by toAccountNumber from the request — throw ResourceNotFoundException if not found.
        BankAccount toAccount = bankAccountRepository.findByAccountNumber(dto.getToAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("To account not found"));

        if (toAccount.getStatus() != BankAccount.AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Recipient account is not active.");
        }

        //  Cannot transfer to the same account.
        if (fromAccount.getAccountNumber().equals(toAccount.getAccountNumber())) {
            throw new InvalidTransactionException("Cannot transfer to the same account.");
        }

        //    Validate fromAccount.balance >= amount — else throw a custom InsufficientBalanceException.
        if (fromAccount.getBalance().compareTo(dto.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        //    Fraud check — use countByFromAccountIdAndCreatedAtAfter(fromAccountId, now.minusHours(1)). If count >= 5,
        //    throw a custom FraudSuspectedException (or similar — more than 5 transactions in the last hour is suspicious).
        List<Transaction> todayTransactions = transactionRepository
                .findAllByFromAccountIdAndCreatedAtBetween(
                        fromAccountId,
                        LocalDateTime.now().withHour(0).withMinute(0).withSecond(0),
                        LocalDateTime.now());

        BigDecimal todayTotal = todayTransactions.stream()
                .filter(t -> t.getTransactionType() == Transaction.TransactionType.TRANSFER)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (todayTotal.add(dto.getAmount()).compareTo(new BigDecimal("100000")) > 0) {
            throw new DailyLimitExceededException("Daily transfer limit of ₹1,00,000 exceeded.");
        }

        if (transactionRepository.countByFromAccountIdAndCreatedAtAfter(fromAccountId, LocalDateTime.now().minusHours(1))
                >= 5) {
            throw new FraudSuspectedException("More than 5 transactions detected in the last hour. Transaction blocked.");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(dto.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(dto.getAmount()));
        bankAccountRepository.save(fromAccount);
        bankAccountRepository.save(toAccount);

        //    Debit fromAccount, credit toAccount. Save both accounts.
        //    Create a Transaction entity — transactionType = "TRANSFER", status = "SUCCESS", generate a unique transactionId (think about how — UUID is a common approach).
        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .balanceAfterTransaction(fromAccount.getBalance())
                .transactionType(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.SUCCESS)
                .build();

        //    Save the transaction.
        //    Map the saved Transaction to TransactionResponseDTO using the builder and return it.
        Transaction savedTransaction = transactionRepository.save(transaction);

        return mapToDto(savedTransaction);
    }

    //    Business rules for deposit():
    @Transactional
    public TransactionResponseDTO deposit(String accountNumber, DepositWithdrawRequestDTO dto) {

        //    Fetch account by accountNumber — throw ResourceNotFoundException if not found.
        BankAccount account = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        String currentUserEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        if (!account.getUser().getEmail().equals(currentUserEmail)) {
            throw new UnauthorizedAccountAccessException("You do not have access to this account.");
        }

        if (account.getStatus() != BankAccount.AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Account is not active.");
        }

        account.setBalance(account.getBalance().add(dto.getAmount()));
        bankAccountRepository.save(account);

        //    Add amount to account balance. Save account.
        //    Create Transaction — transactionType = "DEPOSIT", fromAccountNumber = null, toAccountNumber = accountNumber.
        //    Save transaction, map to DTO, return.
        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .toAccount(account)
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .balanceAfterTransaction(account.getBalance())
                .transactionType(Transaction.TransactionType.CREDIT)
                .status(Transaction.TransactionStatus.SUCCESS)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapToDto(savedTransaction);
    }

    //    Business rules for withdraw():
    @Transactional
    public TransactionResponseDTO withdraw(String accountNumber, DepositWithdrawRequestDTO dto) {

        //    Fetch account by accountNumber — throw ResourceNotFoundException if not found.
        BankAccount account = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        String currentUserEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        if (!account.getUser().getEmail().equals(currentUserEmail)) {
            throw new UnauthorizedAccountAccessException("You do not have access to this account.");
        }

        if (account.getStatus() != BankAccount.AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Account is not active.");
        }

        //    Validate balance >= amount — else InsufficientBalanceException.
        if (account.getBalance().compareTo(dto.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        //    Subtract amount from balance. Save account.
        account.setBalance(account.getBalance().subtract(dto.getAmount()));
        bankAccountRepository.save(account);

        //    Create Transaction — transactionType = "WITHDRAW", fromAccountNumber = accountNumber, toAccountNumber = null.
        //    Save transaction, map to DTO, return.
        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .fromAccount(account)
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .balanceAfterTransaction(account.getBalance())
                .transactionType(Transaction.TransactionType.DEBIT)
                .status(Transaction.TransactionStatus.SUCCESS)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapToDto(savedTransaction);
    }

    private TransactionResponseDTO mapToDto(Transaction transaction) {
        return TransactionResponseDTO.builder()
                .transactionId(transaction.getTransactionId())
                .transactionType(transaction.getTransactionType().name())
                .amount(transaction.getAmount())
                .balanceAfterTransaction(transaction.getBalanceAfterTransaction())
                .description(transaction.getDescription())
                .status(transaction.getStatus().name())
                .fromAccountNumber(transaction.getFromAccount() != null ? transaction.getFromAccount().getAccountNumber() : null)
                .toAccountNumber(transaction.getToAccount() != null ? transaction.getToAccount().getAccountNumber() : null)
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
