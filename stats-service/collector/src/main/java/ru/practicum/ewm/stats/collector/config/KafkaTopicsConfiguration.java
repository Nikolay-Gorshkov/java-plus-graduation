package ru.practicum.ewm.stats.collector.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfiguration {

    @Bean
    public NewTopic userActionsTopic(@Value("${stats.kafka.topics.user-actions}") String topicName) {
        return TopicBuilder.name(topicName).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic eventSimilarityTopic(@Value("${stats.kafka.topics.events-similarity}") String topicName) {
        return TopicBuilder.name(topicName).partitions(3).replicas(1).build();
    }
}
