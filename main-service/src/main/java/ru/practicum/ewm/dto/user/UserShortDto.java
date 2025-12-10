package ru.practicum.ewm.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserShortDto {
    private Long id;

    @NotBlank
    @Size(min = 2, max = 250)
    private String name;
}
