package ru.korshunov.statsclient;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Validated
public interface StatsClient {

    void collectUserAction(@Positive long userId, @Positive long eventId, ActionType actionType);

    default void collectView(@Positive long userId, @Positive long eventId) {
        collectUserAction(userId, eventId, ActionType.VIEW);
    }

    default void collectRegister(@Positive long userId, @Positive long eventId) {
        collectUserAction(userId, eventId, ActionType.REGISTER);
    }

    default void collectLike(@Positive long userId, @Positive long eventId) {
        collectUserAction(userId, eventId, ActionType.LIKE);
    }

    List<RecommendedEvent> getRecommendationsForUser(@Positive long userId, @Positive int maxResults);

    List<RecommendedEvent> getSimilarEvents(@Positive long eventId,
                                            @Positive long userId,
                                            @Positive int maxResults);

    Map<Long, Double> getInteractionsCount(@NotEmpty List<@Positive Long> eventIds);

    default double getInteractionCount(@Positive long eventId) {
        return getInteractionsCount(List.of(eventId)).getOrDefault(eventId, 0.0);
    }

    default List<RecommendedEvent> getRecommendationsForUser(@Positive long userId) {
        return getRecommendationsForUser(userId, 10);
    }

    default List<RecommendedEvent> getSimilarEvents(@Positive long eventId, @Positive long userId) {
        return getSimilarEvents(eventId, userId, 10);
    }
}
