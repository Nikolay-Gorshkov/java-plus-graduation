package ru.practicum.ewm.stats.analyzer.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.analyzer.model.UserEventInteractionEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserEventInteractionRepository extends JpaRepository<UserEventInteractionEntity, Long> {

    Optional<UserEventInteractionEntity> findByUserIdAndEventId(Long userId, Long eventId);

    List<UserEventInteractionEntity> findAllByUserIdOrderByUpdatedAtDesc(Long userId);

    List<UserEventInteractionEntity> findAllByEventIdIn(Collection<Long> eventIds);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    @Query("select i.eventId from UserEventInteractionEntity i where i.userId = :userId")
    Set<Long> findEventIdsByUserId(@Param("userId") Long userId);
}
