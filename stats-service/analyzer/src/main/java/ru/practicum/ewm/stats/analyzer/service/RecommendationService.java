package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.analyzer.model.EventSimilarityEntity;
import ru.practicum.ewm.stats.analyzer.model.UserEventInteractionEntity;
import ru.practicum.ewm.stats.analyzer.repo.EventSimilarityRepository;
import ru.practicum.ewm.stats.analyzer.repo.UserEventInteractionRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private static final int DEFAULT_RECENT_LIMIT = 20;
    private static final int DEFAULT_NEIGHBOR_LIMIT = 10;
    private static final int DEFAULT_CANDIDATE_FACTOR = 5;

    private final UserEventInteractionRepository interactionRepository;
    private final EventSimilarityRepository similarityRepository;

    public List<EventScore> getRecommendationsForUser(long userId, int maxResults) {
        List<UserEventInteractionEntity> interactions = interactionRepository.findAllByUserIdOrderByUpdatedAtDesc(userId);
        if (interactions.isEmpty() || maxResults <= 0) {
            return List.of();
        }

        Map<Long, UserEventInteractionEntity> interactionsByEvent = new LinkedHashMap<>();
        for (UserEventInteractionEntity interaction : interactions) {
            interactionsByEvent.putIfAbsent(interaction.getEventId(), interaction);
        }

        Set<Long> interactedEventIds = interactionsByEvent.keySet();
        Set<Long> recentEventIds = interactions.stream()
                .map(UserEventInteractionEntity::getEventId)
                .distinct()
                .limit(Math.max(maxResults, DEFAULT_RECENT_LIMIT))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        Map<Long, Double> candidateBaseScores = new HashMap<>();
        for (EventSimilarityEntity similarity : similarityRepository.findAllByEventIds(recentEventIds)) {
            Long otherEventId = resolveOtherEvent(similarity, recentEventIds);
            if (otherEventId == null || interactedEventIds.contains(otherEventId) || similarity.getScore() <= 0.0) {
                continue;
            }
            candidateBaseScores.merge(otherEventId, similarity.getScore(), Math::max);
        }

        List<Long> candidateIds = candidateBaseScores.entrySet().stream()
                .sorted(Comparator.<Map.Entry<Long, Double>>comparingDouble(Map.Entry::getValue)
                        .reversed()
                        .thenComparingLong(Map.Entry::getKey))
                .limit((long) maxResults * DEFAULT_CANDIDATE_FACTOR)
                .map(Map.Entry::getKey)
                .toList();

        List<EventScore> predictions = new ArrayList<>();
        for (Long candidateId : candidateIds) {
            List<EventScore> neighbors = similarityRepository.findByEventAOrEventB(candidateId, candidateId).stream()
                    .map(similarity -> {
                        long otherEventId = similarity.getEventA().equals(candidateId)
                                ? similarity.getEventB()
                                : similarity.getEventA();
                        return new EventScore(otherEventId, similarity.getScore());
                    })
                    .filter(neighbor -> interactedEventIds.contains(neighbor.eventId()) && neighbor.score() > 0.0)
                    .sorted(Comparator.comparingDouble(EventScore::score).reversed())
                    .limit(DEFAULT_NEIGHBOR_LIMIT)
                    .toList();

            double weightedSum = 0.0;
            double similaritySum = 0.0;
            for (EventScore neighbor : neighbors) {
                weightedSum += neighbor.score() * interactionsByEvent.get(neighbor.eventId()).getWeight();
                similaritySum += neighbor.score();
            }
            if (similaritySum > 0.0) {
                predictions.add(new EventScore(candidateId, weightedSum / similaritySum));
            }
        }

        return predictions.stream()
                .sorted(Comparator.comparingDouble(EventScore::score).reversed()
                        .thenComparingLong(EventScore::eventId))
                .limit(maxResults)
                .toList();
    }

    public List<EventScore> getSimilarEvents(long eventId, long userId, int maxResults) {
        if (maxResults <= 0) {
            return List.of();
        }
        Set<Long> seenEventIds = interactionRepository.findEventIdsByUserId(userId);
        return similarityRepository.findByEventAOrEventB(eventId, eventId).stream()
                .map(similarity -> {
                    long otherEventId = similarity.getEventA().equals(eventId)
                            ? similarity.getEventB()
                            : similarity.getEventA();
                    return new EventScore(otherEventId, similarity.getScore());
                })
                .filter(score -> score.score() > 0.0)
                .filter(score -> !seenEventIds.contains(score.eventId()))
                .sorted(Comparator.comparingDouble(EventScore::score).reversed()
                        .thenComparingLong(EventScore::eventId))
                .limit(maxResults)
                .toList();
    }

    public List<EventScore> getInteractionsCount(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Double> scoreByEvent = interactionRepository.findAllByEventIdIn(eventIds).stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        UserEventInteractionEntity::getEventId,
                        java.util.stream.Collectors.summingDouble(UserEventInteractionEntity::getWeight)
                ));

        List<EventScore> result = new ArrayList<>(eventIds.size());
        for (Long eventId : eventIds) {
            result.add(new EventScore(eventId, scoreByEvent.getOrDefault(eventId, 0.0)));
        }
        return result;
    }

    private Long resolveOtherEvent(EventSimilarityEntity similarity, Set<Long> sourceEventIds) {
        boolean containsA = sourceEventIds.contains(similarity.getEventA());
        boolean containsB = sourceEventIds.contains(similarity.getEventB());
        if (containsA && !containsB) {
            return similarity.getEventB();
        }
        if (containsB && !containsA) {
            return similarity.getEventA();
        }
        if (containsA) {
            return similarity.getEventB();
        }
        return null;
    }

    public record EventScore(long eventId, double score) {
    }
}
