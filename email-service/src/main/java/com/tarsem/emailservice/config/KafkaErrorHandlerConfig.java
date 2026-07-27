package com.tarsem.emailservice.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
@RequiredArgsConstructor
public class KafkaErrorHandlerConfig {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, ex) -> new TopicPartition(
                                record.topic() + ".DLT",
                                record.partition())
                );

        ExponentialBackOffWithMaxRetries backOff =
                new ExponentialBackOffWithMaxRetries(3);

        backOff.setInitialInterval(2000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(8000L);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}