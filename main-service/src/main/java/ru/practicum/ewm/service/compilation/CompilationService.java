package ru.practicum.ewm.service.compilation;

import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto create(NewCompilationDto newCompilationDto);

    CompilationDto update(Long compId, UpdateCompilationRequest request);

    void delete(Long compId);

    List<CompilationDto> get(Boolean pinned, int from, int size);

    CompilationDto getById(Long compId);
}
