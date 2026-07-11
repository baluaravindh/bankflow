package com.balu.bankflow.service;

import com.balu.bankflow.dto.DepositWithdrawRequestDTO;
import com.balu.bankflow.dto.TransactionEventDTO;
import com.balu.bankflow.dto.TransactionResponseDTO;
import com.balu.bankflow.dto.TransferRequestDTO;
import com.balu.bankflow.entity.BankAccount;
import com.balu.bankflow.entity.Transaction;
import com.balu.bankflow.exception.*;
import com.balu.bankflow.messaging.TransactionEventProducer;
import com.balu.bankflow.messaging.TransactionNotificationProducer;
import com.balu.bankflow.repository.BankAccountRepository;
import com.balu.bankflow.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionEventProducer transactionEventProducer;
    private final TransactionNotificationProducer transactionNotificationProducer;

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

        // Publish Kafka event
        TransactionEventDTO event = buildTransactionEvent(savedTransaction);
        transactionEventProducer.publishTransactionEvent(event);

        // Send RabbitMQ notification (TRANSFER only)
        transactionNotificationProducer.sendTransactionNotification(event);

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

        // Publish Kafka event only
        TransactionEventDTO event = buildTransactionEvent(savedTransaction);
        transactionEventProducer.publishTransactionEvent(event);

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

        // Publish Kafka event only
        TransactionEventDTO event = buildTransactionEvent(savedTransaction);
        transactionEventProducer.publishTransactionEvent(event);

        return mapToDto(savedTransaction);
    }

    // METHOD: getTransactionHistory(String accountNumber, String email)
    // WHO: CUSTOMER (own account only) or BANK_ADMIN
    // WHAT to do:
    // WHAT to return: List<TransactionResponseDTO>
    public List<TransactionResponseDTO> getTransactionHistory(String accountNumber, String email) {
        //   Step 1: Find account by number → throw if not found
        BankAccount account = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        //   Step 2: Check ownership (same as transfer — SecurityContext role check)
        String currentAccountUser = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        if (currentAccountUser.equals("CUSTOMER")) {
            if (!account.getUser().getEmail().equals(email)) {
                throw new UnauthorizedAccountAccessException("You do not have access to this account.");
            }
        }

        //   Step 3: Find all transactions where fromAccount or toAccount = this account
        List<Transaction> transactions = transactionRepository
                .findAllByFromAccountIdOrToAccountId(account.getId(), account.getId());

        //   Step 4: Sort by createdAt descending (latest first)
        //   Step 5: Return list of TransactionResponseDTO
        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .map(this::mapToDto)
                .toList();
    }

    // METHOD: getTransactionsByType(String accountNumber,
    //          String transactionType, String email)
    // WHO: CUSTOMER (own account) or BANK_ADMIN
    // WHAT to do:
    // WHAT to return: List<TransactionResponseDTO>
    public List<TransactionResponseDTO> getTransactionsByType(String accountNumber,
                                                              String transactionType, String email) {

        //   Step 1: Get full transaction history (call getTransactionHistory)
        //   Step 2: Filter by transactionType
        return getTransactionHistory(accountNumber, email)
                .stream()
                .filter(t -> t.getTransactionType().equalsIgnoreCase(transactionType))
                .toList();
    }

    // METHOD: getTransactionsByDateRange(String accountNumber,
    //          LocalDateTime from, LocalDateTime to, String email)
    // WHO: CUSTOMER (own account) or BANK_ADMIN
    // WHAT to do:
    // WHAT to return: List<TransactionResponseDTO>
    public List<TransactionResponseDTO> getTransactionsByDateRange(String accountNumber,
                                                                   LocalDateTime start,
                                                                   LocalDateTime end,
                                                                   String email) {
        //   Step 1: Get full transaction history
        //   Step 2: Filter by createdAt between from and to
        return getTransactionHistory(accountNumber, email)
                .stream()
                .filter(t -> !t.getCreatedAt().isBefore(start) &&
                        !t.getCreatedAt().isAfter(end))
                .toList();
    }

    // METHOD: getTransactionById(String transactionId, String email)
    // WHO: CUSTOMER or BANK_ADMIN
    // WHAT to do:
    // WHAT to return: TransactionResponseDTO
    public TransactionResponseDTO getTransactionById(String transactionId, String email) {

        //   Step 1: Find transaction by transactionId → throw if not found
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        //   Step 2: Verify access — check fromAccount or toAccount belongs to user
        //           (skip check for BANK_ADMIN)
        String role = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities()
                .iterator().next()
                .getAuthority();

        if (role.equals("CUSTOMER")) {
            boolean isFromAccount = transaction.getFromAccount() != null &&
                    transaction.getFromAccount().getUser().getEmail().equals(email);
            boolean isToAccount = transaction.getToAccount() != null &&
                    transaction.getToAccount().getUser().getEmail().equals(email);

            if (!isFromAccount && !isToAccount) {
                throw new UnauthorizedAccountAccessException("You do not have access to this transaction.");
            }
        }

        //   Step 3: Return TransactionResponseDTO
        return mapToDto(transaction);
    }

    private TransactionEventDTO buildTransactionEvent(Transaction transaction) {
        return TransactionEventDTO.builder()
                .transactionId(transaction.getTransactionId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .fromAccountNumber(transaction.getFromAccount() != null ?
                        transaction.getFromAccount().getAccountNumber() : null)
                .toAccountNumber(transaction.getToAccount() != null ?
                        transaction.getToAccount().getAccountNumber() : null)
                .status(transaction.getStatus())
                .timestamp(transaction.getCreatedAt())
                .build();
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
