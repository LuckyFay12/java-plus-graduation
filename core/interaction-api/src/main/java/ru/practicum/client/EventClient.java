package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "event-service", path = "/internal/events", fallback = EventClientFallback.class)
public interface EventClient extends EventOperations {
}
