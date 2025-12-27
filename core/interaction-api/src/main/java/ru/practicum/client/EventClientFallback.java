package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.dto.EventFullDto;

@Component
@Slf4j
public class EventClientFallback implements EventClient {
    @Override
    public EventFullDto getByEventId(Long eventId) {
        log.warn("Fallback triggered for getByEventId({})", eventId);
        throw new RuntimeException("Event Service недоступен");
    }

    @Override
    public void incrementConfirmedRequests(Long eventId, int count) {
        log.warn("Fallback triggered for incrementConfirmedRequests({}, {})", eventId, count);
    }
}