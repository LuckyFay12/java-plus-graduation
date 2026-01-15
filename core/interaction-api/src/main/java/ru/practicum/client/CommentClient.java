package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "comment-service", path = "/internal/comments")
public interface CommentClient extends CommentOperations {
}
