package ru.practicum.ewm.service.request;

import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.entity.request.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface RequestService {
    Optional<ParticipationRequest> findById(Long requestId);

    ParticipationRequest getById(Long requestId);

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
}
