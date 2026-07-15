package com.balu.bankflow.batch;

import com.balu.bankflow.entity.Loan;
import com.balu.bankflow.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LoanItemReader implements ItemReader<Loan> {

    // Inject: LoanRepository
    private final LoanRepository loanRepository;
    private List<Loan> loans;
    private Iterator<Loan> iterator;

    @Nullable
    @Override
    public Loan read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

        if (iterator == null) {
            loans = loanRepository.findByStatus(Loan.LoanStatus.APPROVED);
            iterator = loans.iterator();
        }

        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }
}
