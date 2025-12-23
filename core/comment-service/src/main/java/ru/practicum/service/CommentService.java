package ru.practicum.service;

import ru.practicum.dto.CommentDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.dto.UpdateCommentRequest;
import ru.practicum.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    Optional<Comment> findById(Long commentId);

    Comment getById(Long commentId);

    CommentDto getCommentById(Long commentId);

    Optional<Comment> findByIdAndAuthorId(Long id, Long authorId);

    Comment getByIdAndAuthorId(Long id, Long authorId);

    CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto updateCommentByUser(Long userId, Long commentId, UpdateCommentRequest request);

    void deleteCommentByUser(Long userId, Long commentId);

    List<CommentDto> getUserComments(Long userId, Integer from, Integer size);

    List<CommentDto> getEventComments(Long eventId, Integer from, Integer size);

    CommentDto moderateComment(Long commentId, Boolean approve);

    List<CommentDto> getCommentsForModeration(Integer from, Integer size);

    Long getCountPublishedCommentsByEventId(Long eventId);
}