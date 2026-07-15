package com.balu.bankflow.batch;

import com.balu.bankflow.entity.BankAccount;
import com.balu.bankflow.entity.Loan;
import com.balu.bankflow.entity.Transaction;
import com.balu.bankflow.repository.BankAccountRepository;
import com.balu.bankflow.repository.LoanRepository;
import com.balu.bankflow.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmiDeductionProcessor implements ItemProcessor<Loan, Transaction> {

    // Inject: BankAccountRepository, TransactionRepository (only if needed inside — likely not, writer saves)
    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final LoanRepository loanRepository;

    // METHOD: process(Loan loan)
    //   WHAT to return: the built Transaction (writer will save it)

    @Nullable
    @Override
    public Transaction process(@NonNull Loan loan) throws Exception {

        // WHAT validate:
        //   - If loan.installmentsPaid >= loan.tenure → return null (skip, already fully paid)
        if (loan.getInstallmentsPaid() >= loan.getTenure()) {
            return null;
        }

        //   - Find BankAccount by loan.user → if not found, log.warn, return null (skip)
        BankAccount bankAccount = bankAccountRepository.findByUser(loan.getUser());
        if (bankAccount == null) {
            log.warn("no bank account found for loan {}", loan.getId());
            return null;
        }

        //   - Check bankAccount.balance >= loan.emiAmount
        //     → if insufficient, log.warn "insufficient balance for loan {id}", return null (skip)
        if (bankAccount.getBalance().compareTo(loan.getEmiAmount()) < 0) {
            log.warn("insufficient balance for loan {}", loan.getId());
            return null;
        }

        // WHAT to do:
        //   Step 1: Deduct emiAmount from bankAccount.balance
        //   Step 2: Save bankAccount (via BankAccountRepository)
        bankAccount.setBalance(bankAccount.getBalance().subtract(loan.getEmiAmount()));
        bankAccountRepository.save(bankAccount);

        //   Step 3: Increment loan.installmentsPaid by 1
        loan.setInstallmentsPaid(loan.getInstallmentsPaid() + 1);

        //   Step 4: If installmentsPaid == tenure → set loan.status = CLOSED
        if (loan.getInstallmentsPaid() == loan.getTenure()) {
            loan.setStatus(Loan.LoanStatus.CLOSED);
        }

        //   Step 5: Save loan (via LoanRepository — wait, need to inject this too)
        loanRepository.save(loan);

        //   Step 6: Build a Transaction entity:
        //           - bankAccount = the account
        //           - amount = loan.emiAmount
        //           - type = DEBIT (or whatever enum value your Transaction uses for outgoing)
        //           - description = "EMI payment for loan #" + loan.id
        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .fromAccount(bankAccount)
                .amount(loan.getEmiAmount())
                .balanceAfterTransaction(bankAccount.getBalance())
                .transactionType(Transaction.TransactionType.DEBIT)
                .description("EMI payment for loan #" + loan.getId())
                .build();
//        transactionRepository.save(transaction);

        return transaction;
    }
}
