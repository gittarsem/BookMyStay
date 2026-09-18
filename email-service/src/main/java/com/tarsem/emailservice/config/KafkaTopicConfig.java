package com.tarsem.emailservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic bookingConfirmedTopic() {
        return TopicBuilder.name("booking-confirmed")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bookingExpiredTopic() {
        return TopicBuilder.name("booking-expired")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bookingCancelledTopic() {
        return TopicBuilder.name("booking-cancelled")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bookingConfirmedDltTopic() {
        return TopicBuilder.name("booking-confirmed.DLT")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bookingExpiredDltTopic() {
        return TopicBuilder.name("booking-expired.DLT")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bookingCancelledDltTopic() {
        return TopicBuilder.name("booking-cancelled.DLT")
                .partitions(1)
                .replicas(1)
                .build();
    }
}