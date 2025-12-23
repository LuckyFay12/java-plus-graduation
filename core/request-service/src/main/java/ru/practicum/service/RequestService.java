package ru.practicum.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface RequestService {
    Optional<ParticipationRequest> findById(Long requestId);

    ParticipationRequest getById(Long requestId);

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getRequests(@Positive Long userId, @Positive Long eventId);

    EventRequestStatusUpdateResult updateRequest(@Valid EventRequestStatusUpdateRequest request,
                                                 @Positive Long userId, @Positive Long eventId);
}
