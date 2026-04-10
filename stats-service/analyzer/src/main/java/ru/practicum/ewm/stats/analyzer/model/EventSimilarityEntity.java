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
@Table(name = "event_similarities",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_a", "event_b"}))
public class EventSimilarityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_a", nullable = false)
    private Long eventA;

    @Column(name = "event_b", nullable = false)
    private Long eventB;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
