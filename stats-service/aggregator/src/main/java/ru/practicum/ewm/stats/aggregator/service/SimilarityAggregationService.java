package ru.practicum.ewm.stats.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.AvroSerde;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimilarityAggregationService {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    private final Map<Long, Map<Long, Double>> eventUserWeights = new HashMap<>();
    private final Map<Long, Double> eventWeightSums = new HashMap<>();
    private final Map<Long, Map<Long, Double>> minWeightSums = new HashMap<>();

    @Value("${stats.kafka.topics.events-similarity}")
    private String similarityTopic;

    @KafkaListener(topics = "${stats.kafka.topics.user-actions}",
            groupId = "${spring.application.name}",
            containerFactory = "kafkaListenerContainerFactory")
    public synchronized void onUserAction(byte[] payload) {
        UserActionAvro action = AvroSerde.deserializeUserAction(payload);
        long userId = action.userId();
        long eventId = action.eventId();
        double newWeight = action.actionType().getWeight();

        Map<Long, Double> userWeightsForEvent = eventUserWeights.computeIfAbsent(eventId, key -> new HashMap<>());
        double oldWeight = userWeightsForEvent.getOrDefault(userId, 0.0);
        if (Double.compare(newWeight, oldWeight) <= 0) {
            log.debug("Skip user action because weight did not grow: user={}, event={}, old={}, new={}",
                    userId, eventId, oldWeight, newWeight);
            return;
        }

        userWeightsForEvent.put(userId, newWeight);
        eventWeightSums.merge(eventId, newWeight - oldWeight, Double::sum);

        TreeSet<Long> knownEvents = new TreeSet<>(eventWeightSums.keySet());
        for (Long otherEventId : knownEvents) {
            if (otherEventId.equals(eventId)) {
                continue;
            }

            double otherWeight = eventUserWeights
                    .getOrDefault(otherEventId, Map.of())
                    .getOrDefault(userId, 0.0);
            if (Double.compare(otherWeight, 0.0) <= 0) {
                continue;
            }

            double currentMinSum = getMinWeightSum(eventId, otherEventId);
            double updatedMinSum = currentMinSum + Math.min(newWeight, otherWeight) - Math.min(oldWeight, otherWeight);
            if (Double.compare(updatedMinSum, 0.0) <= 0) {
                continue;
            }
            putMinWeightSum(eventId, otherEventId, updatedMinSum);

            double score = calculateScore(updatedMinSum,
                    eventWeightSums.getOrDefault(eventId, 0.0),
                    eventWeightSums.getOrDefault(otherEventId, 0.0));

            long first = Math.min(eventId, otherEventId);
            long second = Math.max(eventId, otherEventId);
            EventSimilarityAvro similarity = new EventSimilarityAvro(first, second, score, normalizeTimestamp(action));
            kafkaTemplate.send(similarityTopic, first + "-" + second, AvroSerde.serialize(similarity));
            log.debug("Updated similarity: {}-{} -> {}", first, second, score);
        }
    }

    private Instant normalizeTimestamp(UserActionAvro action) {
        return action.timestamp() != null ? action.timestamp() : Instant.now();
    }

    private double calculateScore(double minSum, double firstWeightSum, double secondWeightSum) {
        if (Double.compare(minSum, 0.0) <= 0
                || Double.compare(firstWeightSum, 0.0) <= 0
                || Double.compare(secondWeightSum, 0.0) <= 0) {
            return 0.0;
        }
        return minSum / Math.sqrt(firstWeightSum * secondWeightSum);
    }

    private void putMinWeightSum(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        minWeightSums.computeIfAbsent(first, key -> new HashMap<>()).put(second, sum);
    }

    private double getMinWeightSum(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        return minWeightSums.getOrDefault(first, Map.of()).getOrDefault(second, 0.0);
    }
}
