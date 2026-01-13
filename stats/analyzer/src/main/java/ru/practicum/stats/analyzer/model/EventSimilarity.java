package ru.practicum.stats.analyzer.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "similarities")
public class EventSimilarity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @NotNull
    private Long event1;

    @NotNull
    private Long event2;

    @NotNull
    private Double similarity;

    @Column(name = "ts", nullable = false)
    private Instant timestamp;
}
