package com.balu.bankflow.messaging;

import com.balu.bankflow.dto.TransactionEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionNotificationConsumer {

    // METHOD: consumeTransactionNotification(TransactionEventDTO event)
    // WHO: listens to RabbitMQ queue
    // Annotation: @RabbitListener
    //             queues = "${bf.rabbitmq.queue.transaction}"
    // WHAT to do:
    //   Step 1: log.info notification received with
    //           transactionId, type, fromAccount, toAccount
    // WHAT to return: void
    @RabbitListener(queues = "${bf.rabbitmq.queue.transaction}")
    public void consumeTransactionNotification(TransactionEventDTO dto) {
        log.info("Received RabbitMQ notification - transactionId {}," +
                        "type {}, fromAccount{}, toAccount{}", dto.getTransactionId(), dto.getTransactionType(),
                dto.getFromAccountNumber(), dto.getToAccountNumber());
    }
}
