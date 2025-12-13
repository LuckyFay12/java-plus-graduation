package ru.practicum.ewm.repository.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.entity.event.Location;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByLatAndLon(Float lat, Float lon);

    @Query("SELECT l FROM Location l WHERE l.lat BETWEEN :minLat AND :maxLat " +
            "AND l.lon BETWEEN :minLon AND :maxLon")
    List<Location> findByArea(Float minLat, Float maxLat, Float minLon, Float maxLon);
}
