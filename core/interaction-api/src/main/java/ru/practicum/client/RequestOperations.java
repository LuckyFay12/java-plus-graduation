package ru.practicum.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface RequestOperations {

    @GetMapping
    Boolean existsByRequesterIdAndEventId(@RequestParam Long requesterId,
                                          @RequestParam Long eventId);
}
