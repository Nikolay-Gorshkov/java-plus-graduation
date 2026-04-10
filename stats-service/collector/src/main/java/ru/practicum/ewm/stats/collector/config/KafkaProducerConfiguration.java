package ru.practicum.ewm.stats.collector.config;

import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
public class KafkaProducerConfiguration {

    @Bean
    public ProducerFactory<Long, byte[]> collectorProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> properties = kafkaProperties.buildProducerProperties();
        properties.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                LongSerializer.class);
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<Long, byte[]> collectorKafkaTemplate(ProducerFactory<Long, byte[]> collectorProducerFactory) {
        return new KafkaTemplate<>(collectorProducerFactory);
    }
}
