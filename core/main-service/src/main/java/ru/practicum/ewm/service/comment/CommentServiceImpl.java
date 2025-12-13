package ru.practicum.ewm.service.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;
import ru.practicum.ewm.dto.comment.UpdateCommentRequest;
import ru.practicum.ewm.entity.comment.Comment;
import ru.practicum.ewm.entity.comment.CommentStatus;
import ru.practicum.ewm.entity.event.Event;
import ru.practicum.ewm.entity.event.EventState;
import ru.practicum.ewm.entity.user.User;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CommentMapper;
import ru.practicum.ewm.repository.comment.CommentRepository;
import ru.practicum.ewm.service.event.EventPublicService;
import ru.practicum.ewm.service.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserService userService;
    private final EventPublicService eventPublicService;

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
        User user = userService.getById(userId);
        Event event = eventPublicService.getById(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя комментировать неопубликованное событие");
        }

        if (commentRepository.existsByEventIdAndAuthorId(eventId, userId)) {
            throw new ConflictException("Вы уже оставляли комментарий к этому событию");
        }

        Comment comment = commentMapper.toComment(newCommentDto);
        comment.setAuthor(user);
        comment.setEvent(event);
        comment.setCreated(LocalDateTime.now());
        comment.setStatus(CommentStatus.PENDING);

        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toCommentDto(savedComment);
    }

    @Override
    @Transactional
    public CommentDto updateCommentByUser(Long userId, Long commentId, UpdateCommentRequest request) {
        userService.getById(userId);
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
        userService.getById(userId);
        Comment comment = getByIdAndAuthorId(commentId, userId);

        comment.setStatus(CommentStatus.DELETED);
        commentRepository.save(comment);
    }

    @Override
    public List<CommentDto> getUserComments(Long userId, Integer from, Integer size) {
        userService.getById(userId);
        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findByAuthorId(userId, pageable);

        return comments.stream()
                .map(commentMapper::toCommentDto)
                .toList();
    }

    @Override
    public List<CommentDto> getEventComments(Long eventId, Integer from, Integer size) {
        eventPublicService.getById(eventId);
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
}