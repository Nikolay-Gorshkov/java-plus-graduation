package ru.practicum.ewm.rating.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.rating.model.EventRating;

import java.util.List;
import java.util.Optional;

public interface EventRatingRepository extends JpaRepository<EventRating, Long> {
    Optional<EventRating> findByEventIdAndUserId(Long eventId, Long userId);

    @Query(value = """
            select er.event_id as eventId,
                   coalesce(sum(case when er.value = 1 then 1 else 0 end), 0) as likes,
                   coalesce(sum(case when er.value = -1 then 1 else 0 end), 0) as dislikes,
                   coalesce(sum(er.value), 0) as rating
            from event_ratings er
            where er.event_id in (:eventIds)
            group by er.event_id
            """, nativeQuery = true)
    List<RatingAggregateProjection> getSummaries(List<Long> eventIds);
}
