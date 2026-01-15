package ru.practicum.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.EventFullDto;

public interface EventOperations {

    @GetMapping("/{eventId}")
    EventFullDto getByEventId(@PathVariable Long eventId);

    @PutMapping("/{eventId}/increment")
    void incrementConfirmedRequests(@PathVariable Long eventId,
                                    @RequestParam(required = false, defaultValue = "1") int count);
}
