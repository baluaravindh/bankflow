package com.balu.bankflow.service;

import com.balu.bankflow.dto.AccountResponseDTO;
import com.balu.bankflow.dto.CreateAccountRequestDTO;
import com.balu.bankflow.dto.UpdateAccountStatusRequestDTO;
import com.balu.bankflow.entity.BankAccount;
import com.balu.bankflow.entity.User;
import com.balu.bankflow.exception.ResourceNotFoundException;
import com.balu.bankflow.repository.BankAccountRepository;
import com.balu.bankflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankAccountService {

    // Inject: BankAccountRepository, UserRepository
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    // METHOD: createAccount(String email, CreateAccountRequestDTO dto)
    // WHO: CUSTOMER only
    // WHAT to do:
    // WHAT to return: AccountResponseDTO
    public AccountResponseDTO createAccount(String email, CreateAccountRequestDTO dto) {

        log.info("User details{}", email);
        //   Step 1: Find user by email → throw ResourceNotFoundException
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        //   Step 2: Generate unique account number
        //           use System.currentTimeMillis() + random 4 digits
        //           e.g. "BF" + System.currentTimeMillis() + random(1000,9999)
        //           check existsByAccountNumber → regenerate if exists
        String accountNumberGen = generateAccountNumber();
        if (bankAccountRepository.existsByAccountNumber(accountNumberGen)) {
            accountNumberGen = generateAccountNumber();
        }
        //   Step 3: Build and save BankAccount entity
        BankAccount bankAccount = BankAccount.builder()
                .accountNumber(accountNumberGen)
                .accountType(dto.getAccountType())
                .balance(new BigDecimal("0.00"))
                .status(BankAccount.AccountStatus.ACTIVE)
                .user(user)
                .build();
        BankAccount savedAccount = bankAccountRepository.save(bankAccount);

        //   Step 4: Return AccountResponseDTO
        return mapToDto(savedAccount);
    }

    // METHOD: getMyAccounts(String email)
    // WHO: CUSTOMER only
    // WHAT to do:
    // WHAT to return: List<AccountResponseDTO>
    public List<AccountResponseDTO> getMyAccounts(String email) {

        //   Step 1: Find user by email → throw ResourceNotFoundException
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        //   Step 2: Find all accounts by user
        //   Step 3: Map to list of AccountResponseDTO
        return bankAccountRepository.findAllByUser(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // METHOD: getAccountByNumber(String accountNumber, String email)
    // WHO: CUSTOMER (own account) or BANK_ADMIN
    // WHAT to do:
    // WHAT to return: AccountResponseDTO
    public AccountResponseDTO getAccountByNumber(String accountNumber, String email) {

        //   Step 1: Find account by number → throw ResourceNotFoundException
        BankAccount bankAccount = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        //   Step 2: If CUSTOMER — verify account belongs to them
        //           throw RuntimeException if not their account
        String role = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities()
                .iterator().next()
                .getAuthority();
        if (role.equals("ROLE_CUSTOMER")) {
            if (!bankAccount.getUser().getEmail().equals(email)) {
                throw new RuntimeException("This account does not belong to you.");
            }
        }
        //   Step 3: Return AccountResponseDTO
        return mapToDto(bankAccount);
    }

    // METHOD: getAllAccounts()
    // WHO: BANK_ADMIN only
    // WHAT to do:
    // WHAT to return: List<AccountResponseDTO>
    public List<AccountResponseDTO> getAllAccounts() {
        //   Step 1: Find all accounts
        //   Step 2: Map to list of AccountResponseDTO
        return bankAccountRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // METHOD: updateAccountStatus(String accountNumber,
    //         UpdateAccountStatusRequestDTO dto)
    // WHO: BANK_ADMIN only
    // WHAT to do:

    // WHAT to return: AccountResponseDTO
    public AccountResponseDTO updateAccountStatus(String accountNumber, UpdateAccountStatusRequestDTO dto) {
        //   Step 1: Find account by number → throw ResourceNotFoundException
        BankAccount bankAccount = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        //   Step 2: Set new status
        bankAccount.setStatus(dto.getAccountStatus());

        //   Step 3: Save account
        bankAccountRepository.save(bankAccount);
        //   Step 4: log.info status updated
        log.info("Account {} has been updated", accountNumber);
        return mapToDto(bankAccount);
    }

    // PRIVATE METHOD: generateAccountNumber()
    // WHAT to return: String (unique account number)
    private String generateAccountNumber() {
        int random = (int) (Math.random() * 9000) + 1000;
        return "BF" + System.currentTimeMillis() + random;
    }

    // PRIVATE METHOD: mapToDto(BankAccount account)
    // WHAT to return: AccountResponseDTO
    private AccountResponseDTO mapToDto(BankAccount bankAccount) {
        return AccountResponseDTO.builder()
                .id(bankAccount.getId())
                .accountNumber(bankAccount.getAccountNumber())
                .accountType(bankAccount.getAccountType().name())
                .balance(bankAccount.getBalance())
                .status(bankAccount.getStatus().name())
                .userFullName(bankAccount.getUser().getFullName())
                .userEmail(bankAccount.getUser().getEmail())
                .createdAt(bankAccount.getCreatedAt())
                .build();
    }
}
