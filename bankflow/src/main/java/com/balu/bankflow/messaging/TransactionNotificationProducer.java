package com.balu.bankflow.messaging;

import com.balu.bankflow.dto.TransactionEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionNotificationProducer {

    // Inject: RabbitTemplate
    // @Value bf.rabbitmq.exchange
    // @Value bf.rabbitmq.routing.key.transaction
    private final RabbitTemplate rabbitTemplate;

    @Value("${bf.rabbitmq.exchange}")
    private String exchange;

    @Value("${bf.rabbitmq.routing.key.transaction}")
    private String routingKey;

    // METHOD: sendTransactionNotification(TransactionEventDTO event)
    // WHO: called from TransactionService after TRANSFER only
    // WHAT to do:
    //   Step 1: Send event to RabbitMQ exchange with routing key
    //   Step 2: log.info notification sent
    // WHAT to return: void
    public void sendTransactionNotification(TransactionEventDTO dto) {
        rabbitTemplate.convertAndSend(exchange, routingKey, dto);
        log.info("Transaction notification sent to RabbitMQ for: {}", dto.getTransactionId());
    }
}
