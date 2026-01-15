package ru.practicum.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface CommentOperations {

    @GetMapping("/{eventId}")
    Long getCountPublishedCommentsByEventId(@PathVariable Long eventId);
}
