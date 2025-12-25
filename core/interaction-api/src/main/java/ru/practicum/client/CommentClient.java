package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "comment-service", path = "/internal/comments")
public interface CommentClient {

    @GetMapping("/{eventId}")
    Long getCountPublishedCommentsByEventId(@PathVariable Long eventId);
}
