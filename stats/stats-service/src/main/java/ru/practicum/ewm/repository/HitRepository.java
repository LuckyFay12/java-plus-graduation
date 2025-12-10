package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.dto.ViewStats;
import ru.practicum.ewm.entity.Hit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HitRepository extends JpaRepository<Hit, Integer> {

    @Query("""
            SELECT h.app as app, h.uri as uri, COUNT(h) as hits
            FROM Hit h
            WHERE h.createdAt BETWEEN :start AND :end
            AND (:uris IS NULL OR h.uri IN :uris)
            GROUP BY h.app, h.uri
            ORDER BY COUNT(h) DESC
            """)
    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("""
            SELECT h.app as app, h.uri as uri, COUNT(DISTINCT h.ip) as hits
            FROM Hit h
            WHERE h.createdAt BETWEEN :start AND :end
            AND (:uris IS NULL OR h.uri IN :uris)
            GROUP BY h.app, h.uri
            ORDER BY COUNT(DISTINCT h.ip) DESC
            """)
    List<ViewStats> getStatsUniqueIp(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("""
            SELECT h.app as app, h.uri as uri, COUNT(h) as hits
            FROM Hit h
            WHERE h.createdAt BETWEEN :start AND :end
            GROUP BY h.app, h.uri
            ORDER BY COUNT(h) DESC
            """)
    List<ViewStats> getStatsWithoutUris(LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT h.app as app, h.uri as uri, COUNT(DISTINCT h.ip) as hits
            FROM Hit h
            WHERE h.createdAt BETWEEN :start AND :end
            GROUP BY h.app, h.uri
            ORDER BY COUNT(DISTINCT h.ip) DESC
            """)
    List<ViewStats> getStatsUniqueIpWithoutUris(LocalDateTime start, LocalDateTime end);
}