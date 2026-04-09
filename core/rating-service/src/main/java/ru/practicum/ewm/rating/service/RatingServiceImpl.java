package ru.practicum.ewm.rating.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.dto.EventInternalDto;
import ru.practicum.ewm.client.event.EventServiceClient;
import ru.practicum.ewm.client.user.UserServiceClient;
import ru.practicum.ewm.error.ConflictException;
import ru.practicum.ewm.error.NotFoundException;
import ru.practicum.ewm.event.status.StateEvent;
import ru.practicum.ewm.rating.dto.RatingSummaryDto;
import ru.practicum.ewm.rating.model.EventRating;
import ru.practicum.ewm.rating.model.ReactionType;
import ru.practicum.ewm.rating.repository.EventRatingRepository;
import ru.practicum.ewm.rating.repository.RatingAggregateProjection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingServiceImpl implements RatingService {

    private final EventRatingRepository ratingRepository;
    private final EventServiceClient eventServiceClient;
    private final UserServiceClient userServiceClient;

    @Transactional
    @Override
    public RatingSummaryDto rate(Long userId, Long eventId, ReactionType reaction) {
        userServiceClient.getUser(userId);
        EventInternalDto event = eventServiceClient.getEvent(eventId);

        if (userId.equals(event.getInitiatorId())) {
            throw new ConflictException("Инициатор не может оценивать своё событие");
        }
        if (event.getState() != StateEvent.PUBLISHED) {
            throw new ConflictException("Нельзя голосовать за непубликованное событие");
        }

        EventRating rating = ratingRepository.findByEventIdAndUserId(eventId, userId)
                .orElseGet(() -> EventRating.builder()
                        .eventId(eventId)
                        .userId(userId)
                        .createdAt(LocalDateTime.now())
                        .build());

        rating.setReaction(reaction);
        rating.setValue(reaction.getScore());
        rating.setUpdatedAt(LocalDateTime.now());
        ratingRepository.save(rating);
        return getSummary(eventId);
    }

    @Transactional
    @Override
    public RatingSummaryDto removeRate(Long userId, Long eventId) {
        EventRating rating = ratingRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Реакция не найдена"));
        ratingRepository.delete(rating);
        return getSummary(eventId);
    }

    @Override
    public RatingSummaryDto getSummary(Long eventId) {
        return getSummaries(List.of(eventId)).stream()
                .findFirst()
                .orElseGet(() -> emptySummary(eventId));
    }

    @Override
    public List<RatingSummaryDto> getSummaries(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return List.of();
        }

        Map<Long, RatingSummaryDto> summaries = ratingRepository.getSummaries(eventIds).stream()
                .map(this::toSummary)
                .collect(Collectors.toMap(RatingSummaryDto::getEventId, Function.identity()));

        return eventIds.stream()
                .distinct()
                .map(eventId -> summaries.getOrDefault(eventId, emptySummary(eventId)))
                .toList();
    }

    private RatingSummaryDto toSummary(RatingAggregateProjection projection) {
        return RatingSummaryDto.builder()
                .eventId(projection.getEventId())
                .likes(projection.getLikes())
                .dislikes(projection.getDislikes())
                .rating(projection.getRating())
                .build();
    }

    private RatingSummaryDto emptySummary(Long eventId) {
        return RatingSummaryDto.builder()
                .eventId(eventId)
                .likes(0L)
                .dislikes(0L)
                .rating(0L)
                .build();
    }
}
