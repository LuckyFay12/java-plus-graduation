package ru.practicum.ewm.service.event;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.*;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.entity.event.Event;

import java.util.List;
import java.util.Optional;

public interface EventService {

    List<EventFullDto> getAll(EventAdminFilter adminFilter, Integer from, Integer size);

    EventFullDto update(UpdateEventAdminRequest request, Long eventId);

    List<EventShortDto> getAll(Long userId, Integer from, Integer size);

    EventFullDto create(NewEventDto newEventDto, Long userId);

    EventFullDto getByInitiatorId(Long userId, Long eventId);

    EventFullDto update(UpdateEventUserRequest request, Long userId, Long eventId);

    List<EventShortDto> getAll(EventPublicFilter publicFilter, Integer from, Integer size,
                               HttpServletRequest httpServletRequest);

    EventFullDto getById(Long eventId, HttpServletRequest httpServletRequest);

    Optional<Event> findById(Long eventId);

    Event getById(Long eventId);

    EventFullDto getByEventId(Long eventId);

    void incrementConfirmedRequests(Long eventId, int count);
}
