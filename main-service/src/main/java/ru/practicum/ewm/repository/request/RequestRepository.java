package ru.practicum.ewm.repository.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.entity.request.ParticipationRequest;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findByRequesterId(Long requesterId);

    List<ParticipationRequest> findByEventId(Long eventId);

    @Query("SELECT COUNT(pr) FROM ParticipationRequest pr " +
            "WHERE pr.event.id = :eventId AND pr.status = 'CONFIRMED'")
    Long countConfirmedRequestsByEventId(@Param("eventId") Long eventId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);
}
