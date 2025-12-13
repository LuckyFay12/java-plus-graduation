package ru.practicum.ewm.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.entity.category.Category;
import ru.practicum.ewm.entity.event.Event;
import ru.practicum.ewm.entity.event.EventState;
import ru.practicum.ewm.entity.event.Location;
import ru.practicum.ewm.entity.event.StateAction;
import ru.practicum.ewm.entity.request.ParticipationRequest;
import ru.practicum.ewm.entity.request.RequestStatus;
import ru.practicum.ewm.entity.user.User;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.mapper.LocationMapper;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.repository.event.EventRepository;
import ru.practicum.ewm.repository.event.LocationRepository;
import ru.practicum.ewm.repository.request.RequestRepository;
import ru.practicum.ewm.service.category.CategoryService;
import ru.practicum.ewm.service.user.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventPrivateServiceImpl implements EventPrivateService {

    private final EventRepository eventRepository;
    private final CategoryService categoryService;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final RequestMapper requestMapper;
    private final LocationMapper locationMapper;
    private final UserService userService;
    private final EventPublicService eventPublicService;

    @Override
    public List<EventShortDto> getAll(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable).getContent();
        return events.stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto create(NewEventDto newEventDto, Long userId) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Указана дата начала события в прошлом");
        }
        Category category = categoryService.getCategoryById(newEventDto.getCategory());
        User user = userService.getById(userId);
        Location location = locationRepository.save(locationMapper.toLocation(newEventDto.getLocation()));

        Event event = eventMapper.toEvent(newEventDto, category, user, location);
        event.setCreatedOn(LocalDateTime.now());
        event.setLocation(location);
        event.setConfirmedRequests(0);
        event.setState(EventState.PENDING);
        Event savedEvent = eventRepository.save(event);

        return eventMapper.toEventFullDto(savedEvent);
    }

    @Override
    public EventFullDto getByInitiatorId(Long userId, Long eventId) {
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Событие id = %d не найдено".formatted(eventId)));
        return eventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto update(UpdateEventUserRequest request, Long userId, Long eventId) {
        Event event = checkUpdateEvent(userId, eventId);
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Событие id = %d не отменено и не в состоянии ожидания.".formatted(eventId));
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Время события указано раньше, чем через два часа от текущего момента");
        }
        if (request.getAnnotation() != null && !request.getAnnotation().isBlank()) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(categoryService.getCategoryById(request.getCategory().getId()));
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(locationRepository.save(locationMapper.toLocation(request.getLocation())));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            if (request.getParticipantLimit() < 0) {
                throw new ValidationException("Нельзя установить отрицательное значение лимита");
            }
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            event.setTitle(request.getTitle());
        }
        if (request.getStateAction() == StateAction.CANCEL_REVIEW) {
            event.setState(EventState.CANCELED);
        }
        if (request.getEventDate() != null) {
            setEventDate(event, String.valueOf(request.getEventDate()));
        }
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
                case REJECT_EVENT -> event.setState(EventState.REJECT);
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                case PUBLISH_EVENT -> event.setState(EventState.PUBLISHED);
            }
        }
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId, Long eventId) {
        userService.getById(userId);

        Event event = eventPublicService.getById(eventId);
        checkInitiator(userId, event);

        List<ParticipationRequest> requests = requestRepository.findByEventId(eventId);
        return requests.stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequest(EventRequestStatusUpdateRequest requestDto, Long userId,
                                                        Long eventId) {
        Event event = checkUpdateEvent(userId, eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Событие id = %d не опубликовано".formatted(eventId));
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
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }
        });
        eventRepository.save(event);
        requestRepository.saveAll(requests);
        List<ParticipationRequestDto> confirmedList = confirmedRequests.stream()
                .map(requestMapper::toDto)
                .toList();
        List<ParticipationRequestDto> regectedList = rejectedRequests.stream()
                .map(requestMapper::toDto)
                .toList();
        return new EventRequestStatusUpdateResult(confirmedList, regectedList);
    }

    private Event checkUpdateEvent(Long userId, Long eventId) {
        Event event = eventPublicService.getById(eventId);
        userService.getById(userId);
        checkInitiator(userId, event);

        return event;
    }

    private void setEventDate(Event event, String date) {
        if (date != null) {
            String normalizedDate = date.replace('T', ' ');
            LocalDateTime eventDateTime = LocalDateTime.parse(normalizedDate,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            if (eventDateTime.isBefore(LocalDateTime.now())) {
                throw new ValidationException("Указанная дата уже наступила");
            }
            event.setEventDate(eventDateTime);
        }
    }

    private void checkInitiator(Long userId, Event event) {
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ValidationException("Пользователь id = %d не является создателем события id = %d"
                    .formatted(userId, event.getId()));
        }
    }
}
