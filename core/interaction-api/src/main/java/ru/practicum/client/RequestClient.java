package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "request-service", path = "/internal/requests")
public interface RequestClient {

    @GetMapping
    Boolean existsByRequesterIdAndEventId(@RequestParam Long requesterId,
                                          @RequestParam Long eventId);
}
