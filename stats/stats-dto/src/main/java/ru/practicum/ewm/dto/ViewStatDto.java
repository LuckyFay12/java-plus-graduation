package ru.practicum.ewm.dto;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ViewStatDto {
    private String app;
    private String uri;
    private Long hits;
}
