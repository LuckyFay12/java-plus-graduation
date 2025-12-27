package ru.practicum.ewm.repository.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.entity.event.Event;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findAll(Specification<Event> specification, Pageable pageable);

    Page<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByInitiatorIdAndId(Long userId, Long eventId);

    Boolean existsByCategoryId(Long categoryId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE events SET confirmed_requests = COALESCE(confirmed_requests, 0) + :count WHERE id = :eventId",
            nativeQuery = true)
    int incrementConfirmedRequestsNative(@Param("eventId") Long eventId,
                                         @Param("count") int count);
}


