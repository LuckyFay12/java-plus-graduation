package ru.practicum.stats.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.analyzer.model.Interaction;

import java.util.List;
import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    Optional<Interaction> findByUserIdAndEventId(long userId, long eventId);

    @Query("SELECT SUM(i.rating) FROM Interaction i WHERE i.eventId = :eventId")
    Double sumMaxRatingPerUserByEventId(@Param("eventId") Long eventId);

    @Query("SELECT i FROM Interaction i WHERE i.userId = :userId ORDER BY i.timestamp DESC")
    List<Interaction> findByUserIdOrderByTimestampDesc(@Param("userId") Long userId);
}
