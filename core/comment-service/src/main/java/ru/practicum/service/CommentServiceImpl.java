package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.*;
import ru.practicum.enums.EventState;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.CommentStatus;
import ru.practicum.repository.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    public Optional<Comment> findById(Long commentId) {
        return commentRepository.findById(commentId);
    }

    @Override
    public Comment getById(Long commentId) {
        return findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id = %d не найден".formatted(commentId)));
    }

    @Override
    public CommentDto getCommentById(Long commentId) {
        return commentMapper.toCommentDto(getById(commentId));
    }

    @Override
    public Optional<Comment> findByIdAndAuthorId(Long id, Long authorId) {
        return commentRepository.findByIdAndAuthorId(id, authorId);
    }

    @Override
    public Comment getByIdAndAuthorId(Long id, Long authorId) {
        return findByIdAndAuthorId(id, authorId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id = %d и authorId = %d не найден".formatted(id, authorId)));
    }

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        UserShortDto user = userClient.getUserById(userId);
        EventFullDto event = eventClient.getByEventId(eventId);

        EventState eventState = getEventStateSafe(event.getState());

        if (eventState != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя комментировать неопубликованное событие");
        }

        if (commentRepository.existsByEventIdAndAuthorId(eventId, userId)) {
            throw new ConflictException("Вы уже оставляли комментарий к этому событию");
        }

        Comment comment = commentMapper.toComment(newCommentDto);
        comment.setAuthorId(user.getId());
        comment.setEventId(event.getId());
        comment.setCreated(LocalDateTime.now());
        comment.setStatus(CommentStatus.PENDING);

        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toCommentDto(savedComment);
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

    @Override
    @Transactional
    public CommentDto updateCommentByUser(Long userId, Long commentId, UpdateCommentRequest request) {
        checkExistsUser(userId);
        Comment comment = getByIdAndAuthorId(commentId, userId);

        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new ConflictException("Нельзя редактировать удаленный комментарий");
        }

        commentMapper.updateCommentFromRequest(request, comment);
        comment.setUpdated(LocalDateTime.now());
        // После редактирования сбрасываем статус на базовый
        comment.setStatus(CommentStatus.PENDING);

        Comment updatedComment = commentRepository.save(comment);

        return commentMapper.toCommentDto(updatedComment);
    }

    @Override
    @Transactional
    public void deleteCommentByUser(Long userId, Long commentId) {
        checkExistsUser(userId);
        Comment comment = getByIdAndAuthorId(commentId, userId);

        comment.setStatus(CommentStatus.DELETED);
        commentRepository.save(comment);
    }

    @Override
    public List<CommentDto> getUserComments(Long userId, Integer from, Integer size) {
        checkExistsUser(userId);
        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findByAuthorId(userId, pageable);

        return comments.stream()
                .map(commentMapper::toCommentDto)
                .toList();
    }

    @Override
    public List<CommentDto> getEventComments(Long eventId, Integer from, Integer size) {
        EventFullDto event = eventClient.getByEventId(eventId);
        if (event == null) {
            throw new NotFoundException("Событие с id = %d не найдено".formatted(eventId));
        }
        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findByEventIdAndStatus(eventId, CommentStatus.PUBLISHED, pageable);

        return comments.stream()
                .map(commentMapper::toCommentDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto moderateComment(Long commentId, Boolean approve) {
        Comment comment = getById(commentId);

        if (approve) {
            comment.setStatus(CommentStatus.PUBLISHED);
        } else {
            comment.setStatus(CommentStatus.REJECTED);
        }

        comment.setUpdated(LocalDateTime.now());
        Comment moderatedComment = commentRepository.save(comment);

        return commentMapper.toCommentDto(moderatedComment);
    }

    @Override
    public List<CommentDto> getCommentsForModeration(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findByStatus(CommentStatus.PENDING, pageable);

        return comments.stream()
                .map(commentMapper::toCommentDto)
                .toList();
    }

    @Override
    public Long getCountPublishedCommentsByEventId(Long eventId) {
        return commentRepository.countPublishedCommentsByEventId(eventId);
    }

    private void checkExistsUser(Long userId) {
        UserShortDto user = userClient.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = %d не найден".formatted(userId));
        }
    }
}