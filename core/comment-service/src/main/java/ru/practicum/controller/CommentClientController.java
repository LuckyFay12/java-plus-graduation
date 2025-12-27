package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.client.CommentClient;
import ru.practicum.service.CommentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/comments")
public class CommentClientController implements CommentClient {
    private final CommentService commentService;

    @Override
    public Long getCountPublishedCommentsByEventId(Long eventId) {
        return commentService.getCountPublishedCommentsByEventId(eventId);
    }
}
