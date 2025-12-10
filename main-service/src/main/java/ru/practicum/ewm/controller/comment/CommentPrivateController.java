package ru.practicum.ewm.controller.comment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;
import ru.practicum.ewm.dto.comment.UpdateCommentRequest;
import ru.practicum.ewm.service.comment.CommentService;

import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/comments")
public class CommentPrivateController {
    private final CommentService commentService;

    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("Запрос на создание комментария пользователем {} к событию {}", userId, eventId);
        return commentService.createComment(userId, eventId, newCommentDto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long commentId,
            @RequestBody @Valid UpdateCommentRequest request) {
        log.info("Запрос на обновление комментария {} пользователем {}", commentId, userId);
        return commentService.updateCommentByUser(userId, commentId, request);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long commentId) {
        log.info("Запрос на удаление комментария {} пользователем {}", commentId, userId);
        commentService.deleteCommentByUser(userId, commentId);
    }

    @GetMapping
    public List<CommentDto> getUserComments(
            @PathVariable @Positive Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Запрос на получение комментариев пользователя {}", userId);
        return commentService.getUserComments(userId, from, size);
    }
}