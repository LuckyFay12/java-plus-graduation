package ru.practicum.ewm.repository.category;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.entity.category.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Boolean existsByName(String name);

    Optional<Category> findByName(String name);
}
