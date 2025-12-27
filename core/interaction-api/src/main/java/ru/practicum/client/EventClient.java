package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;

@FeignClient(name = "event-service", path = "/internal/events", fallback = EventClientFallback.class)
public interface EventClient {

    @GetMapping("/{eventId}")
    EventFullDto getByEventId(@PathVariable Long eventId);

    @PutMapping("/{eventId}/increment")
    void incrementConfirmedRequests(@PathVariable Long eventId,
                                    @RequestParam(required = false, defaultValue = "1") int count);
}
