package com.balu.bankflow.batch;

import com.balu.bankflow.entity.Transaction;
import com.balu.bankflow.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionItemWriter implements ItemWriter<Transaction> {

    // Inject: TransactionRepository
    private final TransactionRepository transactionRepository;

    // METHOD: write(Chunk<? extends Transaction> chunk)
    // WHAT to do: save all transactions in the chunk via TransactionRepository.saveAll()

    @Override
    public void write(@NonNull Chunk<? extends Transaction> chunk) throws Exception {
        transactionRepository.saveAll(chunk.getItems());
    }
}
