package ru.practicum.ewm.stats.aggregator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.AvroSerde;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SimilarityAggregationServiceTest {

    private static final String SIMILARITY_TOPIC = "test-similarity-topic";

    private RecordingKafkaTemplate kafkaTemplate;
    private SimilarityAggregationService service;

    @BeforeEach
    void setUp() {
        kafkaTemplate = new RecordingKafkaTemplate();
        service = new SimilarityAggregationService(kafkaTemplate);
        ReflectionTestUtils.setField(service, "similarityTopic", SIMILARITY_TOPIC);
    }

    @Test
    void shouldPublishUpdatedSimilarityWhenEventWeightGrowsButMinWeightSumStaysTheSame() {
        service.onUserAction(serializeAction(1L, 1L, ActionTypeAvro.VIEW, "2026-04-07T00:00:00Z"));

        assertThat(kafkaTemplate.records()).isEmpty();

        service.onUserAction(serializeAction(1L, 2L, ActionTypeAvro.VIEW, "2026-04-07T00:01:00Z"));

        assertThat(kafkaTemplate.records()).hasSize(1);
        SentRecord firstRecord = kafkaTemplate.records().get(0);
        assertThat(firstRecord.topic()).isEqualTo(SIMILARITY_TOPIC);
        assertThat(firstRecord.key()).isEqualTo("1-2");

        EventSimilarityAvro firstSimilarity = AvroSerde.deserializeEventSimilarity(firstRecord.payload());
        assertThat(firstSimilarity.score()).isEqualTo(1.0);
        assertThat(firstSimilarity.timestamp()).isEqualTo(Instant.parse("2026-04-07T00:01:00Z"));

        service.onUserAction(serializeAction(1L, 1L, ActionTypeAvro.REGISTER, "2026-04-07T00:02:00Z"));

        assertThat(kafkaTemplate.records()).hasSize(2);
        SentRecord updatedRecord = kafkaTemplate.records().get(1);
        assertThat(updatedRecord.topic()).isEqualTo(SIMILARITY_TOPIC);
        assertThat(updatedRecord.key()).isEqualTo("1-2");

        EventSimilarityAvro updatedSimilarity = AvroSerde.deserializeEventSimilarity(updatedRecord.payload());
        assertThat(updatedSimilarity.score()).isCloseTo(0.7071067811865475, within(1.0e-9));
        assertThat(updatedSimilarity.timestamp()).isEqualTo(Instant.parse("2026-04-07T00:02:00Z"));
    }

    private byte[] serializeAction(long userId, long eventId, ActionTypeAvro actionType, String timestamp) {
        return AvroSerde.serialize(new UserActionAvro(userId, eventId, actionType, Instant.parse(timestamp)));
    }

    private static final class RecordingKafkaTemplate extends KafkaTemplate<String, byte[]> {

        private final List<SentRecord> records = new ArrayList<>();

        private RecordingKafkaTemplate() {
            super(new DefaultKafkaProducerFactory<>(Map.of()));
        }

        @Override
        public CompletableFuture<SendResult<String, byte[]>> send(String topic, String key, byte[] data) {
            records.add(new SentRecord(topic, key, data));
            return CompletableFuture.completedFuture(null);
        }

        private List<SentRecord> records() {
            return records;
        }
    }

    private record SentRecord(String topic, String key, byte[] payload) {
    }
}
