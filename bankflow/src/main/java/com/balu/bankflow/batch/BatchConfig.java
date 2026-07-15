package com.balu.bankflow.batch;

import com.balu.bankflow.entity.Loan;
import com.balu.bankflow.entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final EmiDeductionProcessor emiDeductionProcessor;
    private final LoanItemReader loanItemReader;
    private final TransactionItemWriter transactionItemWriter;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job emiDeductionJob() {
        return new JobBuilder("emiDeductionJob", jobRepository)
                .start(emiDeductionStep())
                .build();
    }

    @Bean
    public Step emiDeductionStep() {
        return new StepBuilder("emiDeductionStep", jobRepository)
                .<Loan, Transaction>chunk(10, transactionManager)
                .reader(loanItemReader)
                .processor(emiDeductionProcessor)
                .writer(transactionItemWriter)
                .build();
    }
}
