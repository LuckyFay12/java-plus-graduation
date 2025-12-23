package ru.practicum.ewm.controller.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.ewm.service.event.EventService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/events")
public class EventClientController {

    private final EventService eventService;

    @GetMapping("/{eventId}")
    public EventFullDto getByEventId(@PathVariable Long eventId) {
        return eventService.getByEventId(eventId);
    }

    @PutMapping("/{eventId}/increment")
    public void incrementConfirmedRequests(@PathVariable Long eventId,
                                           @RequestParam(required = false, defaultValue = "1") int count) {
        eventService.incrementConfirmedRequests(eventId, count);
    }
}
