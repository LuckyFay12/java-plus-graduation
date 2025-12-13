package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;
import ru.practicum.ewm.dto.comment.UpdateCommentRequest;
import ru.practicum.ewm.entity.comment.Comment;
import ru.practicum.ewm.entity.comment.CommentStatus;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {
    @Mapping(source = "author", target = "author")
    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "status", target = "status", qualifiedByName = "mapCommentStatusToString")
    CommentDto toCommentDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "status", ignore = true)
    Comment toComment(NewCommentDto newCommentDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateCommentFromRequest(UpdateCommentRequest request, @MappingTarget Comment comment);

    @Named("mapCommentStatusToString")
    default String mapCommentStatusToString(CommentStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("mapStringToCommentStatus")
    default CommentStatus mapStringToCommentStatus(String status) {
        return status != null ? CommentStatus.valueOf(status) : null;
    }
}