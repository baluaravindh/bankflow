package com.balu.bankflow.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Constants (public static final String):
    // QUEUE_NAME = "transaction-notification-queue"
    // EXCHANGE_NAME = "transaction-exchange"
    // ROUTING_KEY = "transaction.notification"

    @Value("${bf.rabbitmq.queue.transaction}")
    private String transactionQueue;

    @Value("${bf.rabbitmq.exchange}")
    private String exchange;

    @Value("${bf.rabbitmq.routing.key.transaction}")
    private String routingKey;

    // BEAN: Queue
    @Bean
    public Queue transactionQueue() {
        return new Queue(transactionQueue, true);
    }

    // BEAN: TopicExchange
    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(exchange);
    }

    // BEAN: Binding (bind queue to exchange with routing key)
    @Bean
    public Binding transactionBinding() {
        return BindingBuilder
                .bind(transactionQueue())
                .to(transactionExchange())
                .with(routingKey);
    }
    // BEAN: MessageConverter (Jackson2JsonMessageConverter)
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // BEAN: RabbitTemplate (with connection factory + message converter)
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory  connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return  rabbitTemplate;
    }
}
