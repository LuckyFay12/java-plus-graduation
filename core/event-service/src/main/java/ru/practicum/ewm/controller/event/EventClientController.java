package ru.practicum.ewm.controller.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.EventClient;
import ru.practicum.dto.EventFullDto;
import ru.practicum.ewm.service.event.EventService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/events")
public class EventClientController implements EventClient {

    private final EventService eventService;

    @Override
    public EventFullDto getByEventId(Long eventId) {
        return eventService.getByEventId(eventId);
    }

    @Override
    public void incrementConfirmedRequests(Long eventId, int count) {
        eventService.incrementConfirmedRequests(eventId, count);
    }
}
