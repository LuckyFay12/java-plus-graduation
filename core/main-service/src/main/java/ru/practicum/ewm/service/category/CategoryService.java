package ru.practicum.ewm.service.category;

import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.entity.category.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    CategoryDto create(NewCategoryDto categoryDto);

    CategoryDto update(NewCategoryDto categoryDto, Long categoryId);

    void delete(Long categoryId);

    List<CategoryDto> getAll(Integer from, Integer size);

    CategoryDto getById(Long categoryId);

    Optional<Category> findById(Long categoryId);

    Category getCategoryById(Long categoryId);
}
