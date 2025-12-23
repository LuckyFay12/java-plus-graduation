package ru.practicum.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentDto;
import ru.practicum.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class CommentAdminController {
    private final CommentService commentService;

    @GetMapping("/{commentId}")
    public CommentDto getCommentById(@PathVariable @Positive Long commentId) {
        log.info("Запрос на получение комментария с ID {}", commentId);
        return commentService.getCommentById(commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto moderateComment(
            @PathVariable @Positive Long commentId,
            @RequestParam Boolean approve) {
        log.info("Запрос на модерацию комментария {}", commentId);
        return commentService.moderateComment(commentId, approve);
    }

    @GetMapping("/moderation")
    public List<CommentDto> getCommentsForModeration(
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Запрос на получение комментариев для модерации");
        return commentService.getCommentsForModeration(from, size);
    }
}