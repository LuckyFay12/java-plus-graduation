package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.service.CommentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/comments")
public class CommentClientController {
    private final CommentService commentService;

    @GetMapping("/{eventId}")
    public Long getCountPublishedCommentsByEventId(Long eventId) {
        return commentService.getCountPublishedCommentsByEventId(eventId);
    }
}
