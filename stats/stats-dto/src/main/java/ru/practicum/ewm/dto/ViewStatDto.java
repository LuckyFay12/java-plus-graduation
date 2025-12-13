package ru.practicum.ewm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ViewStatDto {
    @NotBlank
    private String app;

    @NotBlank
    private String uri;

    @NotBlank
    private long hits;
}
