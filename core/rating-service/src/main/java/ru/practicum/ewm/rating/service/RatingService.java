package ru.practicum.ewm.rating.service;

import ru.practicum.ewm.rating.dto.RatingSummaryDto;
import ru.practicum.ewm.rating.model.ReactionType;

import java.util.List;

public interface RatingService {
    RatingSummaryDto rate(Long userId, Long eventId, ReactionType reaction);

    RatingSummaryDto removeRate(Long userId, Long eventId);

    RatingSummaryDto getSummary(Long eventId);

    List<RatingSummaryDto> getSummaries(List<Long> eventIds);
}
