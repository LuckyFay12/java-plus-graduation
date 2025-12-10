package ru.practicum.ewm.controller.compilation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.service.compilation.CompilationService;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
public class CompilationAdminController {

    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.info("Поступил HTTP-запрос на создание подборки событий: {}", newCompilationDto);
        CompilationDto compilationDto = compilationService.create(newCompilationDto);
        log.info("Успешно обработан HTTP-запрос на создание подборки: {}", compilationDto);
        return compilationDto;
    }

    @PatchMapping("/{compId}")
    public CompilationDto update(@PathVariable @Positive Long compId,
                                 @RequestBody @Valid UpdateCompilationRequest request) {
        log.info("Поступил HTTP-запрос на обновление подборки событий с id {}", compId);
        return compilationService.update(compId, request);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long compId) {
        log.info("Поступил HTTP-запрос на удаление подборки событий с id {}", compId);
        compilationService.delete(compId);
    }
}
