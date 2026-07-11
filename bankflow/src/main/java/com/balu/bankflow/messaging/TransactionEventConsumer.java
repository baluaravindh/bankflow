package com.balu.bankflow.messaging;

import com.balu.bankflow.dto.TransactionEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionEventConsumer {

    // METHOD: consumeTransactionEvent(TransactionEventDTO event)
    // WHO: listens to Kafka topic bf.transaction.status.topic
    // Annotation: @KafkaListener
    //             topics = "${bf.kafka.topic.transaction-events}"
    //             groupId = "${spring.kafka.consumer.group-id}"
    // WHAT to do:
    //   Step 1: log.info event received with transactionId,
    //           type and status
    // WHAT to return: void

    @KafkaListener(
            topics = "${bf.kafka.topic.transaction-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )

    public void consumeTransactionEvent(TransactionEventDTO dto) {
        log.info("Received Kafka transaction event - type: {}, status{}", dto.getTransactionType(), dto.getStatus());
    }
}
