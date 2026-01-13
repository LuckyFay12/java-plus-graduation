package ru.practicum.stats.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.analyzer.model.EventSimilarity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    Optional<EventSimilarity> findByEvent1AndEvent2(Long event1, Long event2);

    @Query("SELECT es FROM EventSimilarity es WHERE " +
           "es.event1 = :eventId OR es.event2 = :eventId " +
           "ORDER BY es.similarity DESC LIMIT :limit")
    List<EventSimilarity> findByEventIdOrderBySimilarityDesc(@Param("eventId") Long eventId,
                                                             @Param("limit") int limit);

    @Query("SELECT es FROM EventSimilarity es WHERE " +
           "es.event1 IN :eventIds OR es.event2 IN :eventIds")
    List<EventSimilarity> findByEventIdIn(@Param("eventIds") Set<Long> eventIds);
}
