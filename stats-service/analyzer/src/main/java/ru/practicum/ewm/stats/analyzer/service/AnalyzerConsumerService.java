package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.analyzer.model.EventSimilarityEntity;
import ru.practicum.ewm.stats.analyzer.model.UserEventInteractionEntity;
import ru.practicum.ewm.stats.analyzer.repo.EventSimilarityRepository;
import ru.practicum.ewm.stats.analyzer.repo.UserEventInteractionRepository;
import ru.practicum.ewm.stats.avro.AvroSerde;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzerConsumerService {

    private final UserEventInteractionRepository interactionRepository;
    private final EventSimilarityRepository similarityRepository;

    @Transactional
    @KafkaListener(topics = "${stats.kafka.topics.user-actions}",
            groupId = "${spring.application.name}",
            containerFactory = "kafkaListenerContainerFactory")
    public void onUserAction(byte[] payload) {
        UserActionAvro action = AvroSerde.deserializeUserAction(payload);
        double newWeight = action.actionType().getWeight();

        UserEventInteractionEntity entity = interactionRepository
                .findByUserIdAndEventId(action.userId(), action.eventId())
                .orElseGet(() -> UserEventInteractionEntity.builder()
                        .userId(action.userId())
                        .eventId(action.eventId())
                        .weight(0.0)
                        .updatedAt(Instant.EPOCH)
                        .build());

        if (Double.compare(newWeight, entity.getWeight()) > 0) {
            entity.setWeight(newWeight);
        }
        if (action.timestamp() != null && action.timestamp().isAfter(entity.getUpdatedAt())) {
            entity.setUpdatedAt(action.timestamp());
        }

        interactionRepository.save(entity);
        log.debug("Stored user interaction: user={}, event={}, weight={}",
                entity.getUserId(), entity.getEventId(), entity.getWeight());
    }

    @Transactional
    @KafkaListener(topics = "${stats.kafka.topics.events-similarity}",
            groupId = "${spring.application.name}",
            containerFactory = "kafkaListenerContainerFactory")
    public void onSimilarity(byte[] payload) {
        EventSimilarityAvro similarity = AvroSerde.deserializeEventSimilarity(payload);
        long first = Math.min(similarity.eventA(), similarity.eventB());
        long second = Math.max(similarity.eventA(), similarity.eventB());

        EventSimilarityEntity entity = similarityRepository.findByEventAAndEventB(first, second)
                .orElseGet(() -> EventSimilarityEntity.builder()
                        .eventA(first)
                        .eventB(second)
                        .build());
        entity.setScore(similarity.score());
        entity.setUpdatedAt(similarity.timestamp());
        similarityRepository.save(entity);
        log.debug("Stored similarity: {}-{} -> {}", first, second, similarity.score());
    }
}
