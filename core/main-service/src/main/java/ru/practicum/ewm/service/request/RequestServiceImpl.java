package ru.practicum.ewm.service.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.entity.event.Event;
import ru.practicum.ewm.entity.event.EventState;
import ru.practicum.ewm.entity.request.ParticipationRequest;
import ru.practicum.ewm.entity.request.RequestStatus;
import ru.practicum.ewm.entity.user.User;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.repository.request.RequestRepository;
import ru.practicum.ewm.service.event.EventPublicService;
import ru.practicum.ewm.service.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserService userService;
    private final EventPublicService eventPublicService;
    private final RequestMapper requestMapper;

    @Override
    public Optional<ParticipationRequest> findById(Long requestId) {
        return requestRepository.findById(requestId);
    }

    @Override
    public ParticipationRequest getById(Long requestId) {
        return findById(requestId).orElseThrow(() -> new NotFoundException("Запрос с id = %d не найден".formatted(requestId)));
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = userService.getById(userId);

        Event event = eventPublicService.getById(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события(userId = %d) не может подать заявку на участие в собственном событии(eventId = %d)".formatted(userId, eventId));
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии(eventId = %d)".formatted(eventId));
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Заявка на участие в этом событии(eventId = %d) уже существует от пользователя(userId = %d)".formatted(eventId, userId));
        }

        if (event.getParticipantLimit() > 0) {
            Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(eventId);
            if (confirmedRequests >= event.getParticipantLimit()) {
                throw new ConflictException("Достигнут лимит участников для этого события");
            }
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(RequestStatus.PENDING)
                .build();

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        ParticipationRequest savedRequest = requestRepository.save(request);
        return requestMapper.toDto(savedRequest);
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userService.getById(userId);

        List<ParticipationRequest> requests = requestRepository.findByRequesterId(userId);
        return requests.stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = getById(requestId);

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Запрос с id = %d не найден для пользователя с userId = %d"
                    .formatted(userId, requestId));
        }

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest updatedRequest = requestRepository.save(request);

        return requestMapper.toDto(updatedRequest);
    }
}
