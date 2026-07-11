package com.balu.bankflow.messaging;

import com.balu.bankflow.dto.TransactionEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventProducer {

    // Inject: KafkaTemplate<String, TransactionEventDTO>
    // @Value bf.kafka.topic.transaction-events
    private final KafkaTemplate<String, TransactionEventDTO> kafkaTemplate;

    @Value("${bf.kafka.topic.transaction-events}")
    private String transactionEvents;

    // METHOD: publishTransactionEvent(TransactionEventDTO event)
    // WHO: called from TransactionService after every transaction
    // WHAT to do:
    // WHAT to return: void
    public void publishTransactionEvent(TransactionEventDTO dto) {

        //   Step 1: Send event to Kafka topic
        //           key = event.getTransactionId()
        //           value = event
        kafkaTemplate.send(transactionEvents, dto.getTransactionId(), dto);

        //   Step 2: log.info event published
        log.info("Transaction event published to Kafka: {}", dto.getTransactionId());
    }
}
