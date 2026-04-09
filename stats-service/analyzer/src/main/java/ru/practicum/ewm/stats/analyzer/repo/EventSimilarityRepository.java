package ru.practicum.ewm.stats.analyzer.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.analyzer.model.EventSimilarityEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarityEntity, Long> {

    Optional<EventSimilarityEntity> findByEventAAndEventB(Long eventA, Long eventB);

    List<EventSimilarityEntity> findByEventAOrEventB(Long eventA, Long eventB);

    @Query("""
            select s
            from EventSimilarityEntity s
            where s.eventA in :eventIds or s.eventB in :eventIds
            """)
    List<EventSimilarityEntity> findAllByEventIds(@Param("eventIds") Collection<Long> eventIds);
}
