package ru.practicum.ewm.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatRequest {
    LocalDateTime start;
    LocalDateTime end;
    List<String> uris;
    Boolean unique;

    public boolean isValid() {
        return start != null && end != null && !start.isAfter(end);
    }
}
