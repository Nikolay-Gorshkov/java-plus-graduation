package ru.practicum.ewm.stats.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_event_interactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_id"}))
public class UserEventInteractionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
