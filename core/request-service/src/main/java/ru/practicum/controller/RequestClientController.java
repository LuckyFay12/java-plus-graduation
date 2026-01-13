package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.client.RequestClient;
import ru.practicum.service.RequestService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/requests")
public class RequestClientController implements RequestClient {

    private final RequestService requestService;

    @Override
    public Boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId) {
        return requestService.existsByRequesterIdAndEventId(requesterId, eventId);
    }
}
