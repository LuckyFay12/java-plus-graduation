package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.ViewStats;
import ru.practicum.ewm.entity.Hit;
import ru.practicum.ewm.mapper.HitMapper;
import ru.practicum.ewm.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final HitRepository hitRepository;
    private final HitMapper hitMapper;

    @Override
    @Transactional
    public EndpointHit saveHit(EndpointHit hitDto) {
        log.info("Saving hit: {}", hitDto);
        Hit hit = hitMapper.toEntity(hitDto);
        hit.setCreatedAt(hitDto.getTimestamp());
        return hitMapper.toDto(hitRepository.save(hit));
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        log.info("Getting stats: start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        List<ViewStats> rows;

        if (uris == null || uris.isEmpty()) {
            if (unique) {
                rows = hitRepository.getStatsUniqueIpWithoutUris(start, end);
            } else {
                rows = hitRepository.getStatsWithoutUris(start, end);
            }
        } else {
            if (unique) {
                rows = hitRepository.getStatsUniqueIp(start, end, uris);
            } else {
                rows = hitRepository.getStats(start, end, uris);
            }
        }

        return rows;
    }
}
