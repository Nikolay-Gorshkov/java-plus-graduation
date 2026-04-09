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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    @Test
    void shouldReplayCiScenarioAroundMissingAndExtraSimilarityMessages() {
        List<TestAction> actions = Arrays.asList(
                action(1L, 9L, ActionTypeAvro.VIEW, "2026-04-07T00:00:00Z"),
                action(4L, 7L, ActionTypeAvro.REGISTER, "2026-04-07T00:01:00Z"),
                action(2L, 9L, ActionTypeAvro.VIEW, "2026-04-07T00:02:00Z"),
                action(3L, 8L, ActionTypeAvro.REGISTER, "2026-04-07T00:03:00Z"),
                action(2L, 8L, ActionTypeAvro.VIEW, "2026-04-07T00:04:00Z"),
                action(5L, 5L, ActionTypeAvro.REGISTER, "2026-04-07T00:05:00Z"),
                action(2L, 6L, ActionTypeAvro.VIEW, "2026-04-07T00:06:00Z"),
                action(3L, 2L, ActionTypeAvro.VIEW, "2026-04-07T00:07:00Z"),
                action(1L, 3L, ActionTypeAvro.REGISTER, "2026-04-07T00:08:00Z"),
                action(6L, 8L, ActionTypeAvro.REGISTER, "2026-04-07T00:09:00Z"),
                action(6L, 8L, ActionTypeAvro.LIKE, "2026-04-07T00:10:00Z"),
                action(1L, 8L, ActionTypeAvro.VIEW, "2026-04-07T00:11:00Z"),
                action(4L, 9L, ActionTypeAvro.VIEW, "2026-04-07T00:12:00Z"),
                action(6L, 1L, ActionTypeAvro.VIEW, "2026-04-07T00:13:00Z"),
                action(6L, 3L, ActionTypeAvro.REGISTER, "2026-04-07T00:14:00Z"),
                action(1L, 7L, ActionTypeAvro.REGISTER, "2026-04-07T00:15:00Z"),
                action(6L, 4L, ActionTypeAvro.REGISTER, "2026-04-07T00:16:00Z"),
                action(6L, 5L, ActionTypeAvro.REGISTER, "2026-04-07T00:17:00Z"),
                action(4L, 10L, ActionTypeAvro.VIEW, "2026-04-07T00:18:00Z"),
                action(2L, 4L, ActionTypeAvro.REGISTER, "2026-04-07T00:19:00Z"),
                action(2L, 1L, ActionTypeAvro.REGISTER, "2026-04-07T00:20:00Z"),
                action(5L, 4L, ActionTypeAvro.VIEW, "2026-04-07T00:21:00Z"),
                action(1L, 5L, ActionTypeAvro.REGISTER, "2026-04-07T00:22:00Z"),
                action(3L, 3L, ActionTypeAvro.REGISTER, "2026-04-07T00:23:00Z"),
                action(6L, 4L, ActionTypeAvro.LIKE, "2026-04-07T00:24:00Z"),
                action(3L, 2L, ActionTypeAvro.VIEW, "2026-04-07T00:25:00Z"),
                action(4L, 6L, ActionTypeAvro.VIEW, "2026-04-07T00:26:00Z"),
                action(1L, 5L, ActionTypeAvro.VIEW, "2026-04-07T00:27:00Z"),
                action(5L, 2L, ActionTypeAvro.VIEW, "2026-04-07T00:28:00Z"),
                action(6L, 10L, ActionTypeAvro.VIEW, "2026-04-07T00:29:00Z"),
                action(3L, 7L, ActionTypeAvro.VIEW, "2026-04-07T00:30:00Z"),
                action(2L, 4L, ActionTypeAvro.LIKE, "2026-04-07T00:31:00Z"),
                action(1L, 12L, ActionTypeAvro.VIEW, "2026-04-07T00:32:00Z"),
                action(6L, 5L, ActionTypeAvro.VIEW, "2026-04-07T00:33:00Z"),
                action(6L, 8L, ActionTypeAvro.VIEW, "2026-04-07T00:34:00Z"),
                action(6L, 3L, ActionTypeAvro.LIKE, "2026-04-07T00:35:00Z"),
                action(1L, 1L, ActionTypeAvro.VIEW, "2026-04-07T00:36:00Z"),
                action(1L, 3L, ActionTypeAvro.LIKE, "2026-04-07T00:37:00Z"),
                action(2L, 9L, ActionTypeAvro.REGISTER, "2026-04-07T00:38:00Z"),
                action(6L, 12L, ActionTypeAvro.VIEW, "2026-04-07T00:39:00Z"),
                action(3L, 11L, ActionTypeAvro.VIEW, "2026-04-07T00:40:00Z"),
                action(6L, 6L, ActionTypeAvro.VIEW, "2026-04-07T00:41:00Z")
        );

        for (TestAction action : actions) {
            service.onUserAction(action.payload());
        }

        int beforeEvent2 = kafkaTemplate.records().size();
        service.onUserAction(action(6L, 2L, ActionTypeAvro.VIEW, "2026-04-07T00:42:00Z").payload());
        List<String> event2Keys = kafkaTemplate.records()
                .subList(beforeEvent2, kafkaTemplate.records().size())
                .stream()
                .map(SentRecord::key)
                .toList();
        assertThat(event2Keys).containsExactly("1-2", "2-3", "2-4", "2-5", "2-6", "2-8", "2-10", "2-12");

        int beforeEvent5Like = kafkaTemplate.records().size();
        service.onUserAction(action(6L, 5L, ActionTypeAvro.LIKE, "2026-04-07T00:43:00Z").payload());
        List<String> event5LikeKeys = kafkaTemplate.records()
                .subList(beforeEvent5Like, kafkaTemplate.records().size())
                .stream()
                .map(SentRecord::key)
                .toList();
        assertThat(event5LikeKeys).containsExactly("1-5", "2-5", "3-5", "4-5", "5-6", "5-8", "5-10", "5-12");
    }

    @Test
    void shouldWaitForSimilarityMessagesToBeAcknowledgedBeforeReturning() throws ExecutionException, InterruptedException, TimeoutException {
        BlockingKafkaTemplate blockingKafkaTemplate = new BlockingKafkaTemplate();
        service = new SimilarityAggregationService(blockingKafkaTemplate);
        ReflectionTestUtils.setField(service, "similarityTopic", SIMILARITY_TOPIC);

        service.onUserAction(serializeAction(1L, 1L, ActionTypeAvro.VIEW, "2026-04-07T00:00:00Z"));

        CompletableFuture<Void> processing = CompletableFuture.runAsync(
                () -> service.onUserAction(serializeAction(1L, 2L, ActionTypeAvro.VIEW, "2026-04-07T00:01:00Z"))
        );

        Thread.sleep(200);
        assertThat(processing).isNotDone();

        blockingKafkaTemplate.completePendingSends();

        processing.get(1, TimeUnit.SECONDS);
        assertThat(processing).isDone();
    }

    private byte[] serializeAction(long userId, long eventId, ActionTypeAvro actionType, String timestamp) {
        return AvroSerde.serialize(new UserActionAvro(userId, eventId, actionType, Instant.parse(timestamp)));
    }

    private static TestAction action(long userId, long eventId, ActionTypeAvro actionType, String timestamp) {
        return new TestAction(AvroSerde.serialize(new UserActionAvro(userId, eventId, actionType, Instant.parse(timestamp))));
    }

    private static class RecordingKafkaTemplate extends KafkaTemplate<String, byte[]> {

        protected final List<SentRecord> records = new ArrayList<>();

        private RecordingKafkaTemplate() {
            super(new DefaultKafkaProducerFactory<>(Map.of()));
        }

        @Override
        public CompletableFuture<SendResult<String, byte[]>> send(String topic, String key, byte[] data) {
            records.add(new SentRecord(topic, key, data));
            return CompletableFuture.completedFuture(null);
        }

        protected List<SentRecord> records() {
            return records;
        }
    }

    private static final class BlockingKafkaTemplate extends RecordingKafkaTemplate {

        private final List<CompletableFuture<SendResult<String, byte[]>>> pendingSends = new ArrayList<>();

        @Override
        public CompletableFuture<SendResult<String, byte[]>> send(String topic, String key, byte[] data) {
            records().add(new SentRecord(topic, key, data));
            CompletableFuture<SendResult<String, byte[]>> future = new CompletableFuture<>();
            pendingSends.add(future);
            return future;
        }

        private void completePendingSends() {
            pendingSends.forEach(future -> future.complete(null));
            pendingSends.clear();
        }
    }

    private record SentRecord(String topic, String key, byte[] payload) {
    }

    private record TestAction(byte[] payload) {
    }
}
