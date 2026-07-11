package com.balu.bankflow.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${bf.kafka.topic.transaction-events}")
    private String transactionEvents;

    @Bean
    public NewTopic transaction(){
        return TopicBuilder
                .name(transactionEvents)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
