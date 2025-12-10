package ru.practicum.ewm.service.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.repository.comment.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentCountService {
    private final CommentRepository commentRepository;

    public Long getCountPublishedCommentsByEventId(Long eventId) {
        return commentRepository.countPublishedCommentsByEventId(eventId);
    }
}
