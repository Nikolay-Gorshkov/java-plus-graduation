package ru.practicum.ewm.rating.repository;

public interface RatingAggregateProjection {
    Long getEventId();

    Long getLikes();

    Long getDislikes();

    Long getRating();
}
