package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Comment;
import ru.practicum.model.CommentStatus;


import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByEventIdAndStatus(Long eventId, CommentStatus status, Pageable pageable);

    List<Comment> findByAuthorId(Long authorId, Pageable pageable);

    Optional<Comment> findByIdAndAuthorId(Long id, Long authorId);

    boolean existsByEventIdAndAuthorId(Long eventId, Long authorId);

    @Query("SELECT c FROM Comment c WHERE c.status = :status ORDER BY c.created DESC")
    List<Comment> findByStatus(@Param("status") CommentStatus status, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.eventId = :eventId AND c.status = 'PUBLISHED'")
    Long countPublishedCommentsByEventId(@Param("eventId") Long eventId);
}