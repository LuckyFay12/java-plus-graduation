package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.*;
import ru.practicum.enums.EventState;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.enums.RequestStatus;
import ru.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserClient userClient;
    private final RequestMapper requestMapper;
    private final EventClient eventClient;

    @Override
    @Transactional(readOnly = true)
    public Optional<ParticipationRequest> findById(Long requestId) {
        return requestRepository.findById(requestId);
    }

    @Override
    @Transactional(readOnly = true)
    public ParticipationRequest getById(Long requestId) {
        return findById(requestId).orElseThrow(() -> new NotFoundException("Запрос с id = %d не найден".formatted(requestId)));
    }

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        checkExistsUser(userId);

        EventFullDto event = eventClient.getByEventId(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события(userId = %d) не может подать заявку на участие в собственном событии(eventId = %d)"
                    .formatted(userId, eventId));
        }

        if (!"PUBLISHED".equals(event.getState())) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии(eventId = %d)".formatted(eventId));
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Заявка на участие в этом событии(eventId = %d) уже существует от пользователя(userId = %d)"
                    .formatted(eventId, userId));
        }

        if (event.getParticipantLimit() > 0) {
            Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(eventId);
            if (confirmedRequests >= event.getParticipantLimit()) {
                throw new ConflictException("Достигнут лимит участников для этого события");
            }
        }
        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .eventId(eventId)
                .requesterId(userId)
                .status(RequestStatus.PENDING)
                .build();

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
            try {
                eventClient.incrementConfirmedRequests(eventId, 1);
            } catch (Exception e) {
                log.error("Ошибка при обновлении счетчика confirmed_requests", e);
            }
        }

        ParticipationRequest savedRequest = requestRepository.save(request);
        return requestMapper.toDto(savedRequest);
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        checkExistsUser(userId);

        List<ParticipationRequest> requests = requestRepository.findByRequesterId(userId);
        return requests.stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = getById(requestId);

        if (!request.getRequesterId().equals(userId)) {
            throw new NotFoundException("Запрос с id = %d не найден для пользователя с userId = %d"
                    .formatted(userId, requestId));
        }

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest updatedRequest = requestRepository.save(request);

        return requestMapper.toDto(updatedRequest);
    }

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId, Long eventId) {
        checkExistsUser(userId);

        EventFullDto event = eventClient.getByEventId(eventId);

        checkInitiator(userId, event);

        List<ParticipationRequest> requests = requestRepository.findByEventId(eventId);
        return requests.stream()
                .map(requestMapper::toDto)
                .toList();
    }

    private void checkInitiator(Long userId, EventFullDto event) {
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ValidationException("Пользователь id = %d не является создателем события id = %d"
                    .formatted(userId, event.getId()));
        }
    }

    @Override
    public EventRequestStatusUpdateResult updateRequest(EventRequestStatusUpdateRequest requestDto, Long userId,
                                                        Long eventId) {
        EventFullDto event = checkUpdateEvent(userId, eventId);
        EventState eventState = getEventStateSafe(event.getState());
        if (eventState != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя комментировать неопубликованное событие");
        }
        if (event.getConfirmedRequests() != null) {
            if (RequestStatus.CONFIRMED.equals(requestDto.getStatus())
                && event.getConfirmedRequests() >= event.getParticipantLimit()) {
                throw new ConflictException("Достигнут лимит заявок");
            }
        }
        List<ParticipationRequest> requests = requestRepository.findAllById(requestDto.getRequestIds());
        List<ParticipationRequest> confirmedRequests = new ArrayList<>();
        List<ParticipationRequest> rejectedRequests = new ArrayList<>();

        requests.forEach(request -> {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Статус не в состоянии ожидания");
            }

            if (event.getConfirmedRequests() < event.getParticipantLimit() && requestDto.getStatus() == RequestStatus.CONFIRMED) {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(request);
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }
        });
        eventClient.incrementConfirmedRequests(eventId, confirmedRequests.size());
        requestRepository.saveAll(requests);

        List<ParticipationRequestDto> confirmedList = confirmedRequests.stream()
                .map(requestMapper::toDto)
                .toList();
        List<ParticipationRequestDto> regectedList = rejectedRequests.stream()
                .map(requestMapper::toDto)
                .toList();
        return new EventRequestStatusUpdateResult(confirmedList, regectedList);
    }

    private EventState getEventStateSafe(String state) {
        if (state == null) {
            return null;
        }
        try {
            return EventState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private EventFullDto checkUpdateEvent(Long userId, Long eventId) {
        EventFullDto event = eventClient.getByEventId(eventId);
        checkExistsUser(userId);
        checkInitiator(userId, event);

        return event;
    }

    private void checkExistsUser(Long userId) {
        UserShortDto user = userClient.getUserById(userId);
        log.info("Получен пользователь {}", user);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = %d не найден".formatted(userId));
        }
    }
}
