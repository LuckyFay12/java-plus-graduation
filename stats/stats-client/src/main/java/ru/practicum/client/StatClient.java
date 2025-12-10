package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.StatRequest;
import ru.practicum.ewm.dto.ViewStatDto;
import ru.practicum.ewm.exception.ApiError;
import ru.practicum.utils.ResponseGenerator;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class StatClient extends ResponseGenerator {

    private final RestClient restClient;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${stat-svc-service.url}")
    private String statServiceUrl;

    public StatClient(@Value("${stat-svc-service.url}") String statServiceUrl) {
        restClient = RestClient.builder()
                .baseUrl(statServiceUrl)
                .build();
    }

    public ResponseEntity<Object> saveHit(EndpointHit hit) {
        try {
            log.info("Сохранение информации о запросе {}", hit);
            return makeResult(restClient.post()
                    .uri("/hit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(hit)
                    .retrieve()
                    .body(EndpointHit.class), HttpStatus.CREATED);
        } catch (Exception e) {
            String msg = "Oшибка при сохранении информации";
            log.error(msg + " {}", e.getMessage(), e);
            return makeResult(ApiError.builder()
                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<ViewStatDto> getStats(StatRequest request) {
        if (request == null || !request.isValid()) {
            log.warn("Некорректные параметры запроса статистики");
            return Collections.emptyList();
        }

        try {
            log.info("Запрос статистики {}", request);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(statServiceUrl + "/stats")
                    .queryParam("start", request.getStart().format(FORMATTER))
                    .queryParam("end", request.getEnd().format(FORMATTER))
                    .queryParam("unique", request.getUnique());

            if (request.getUris() != null && !request.getUris().isEmpty()) {
                String uris = String.join(",", request.getUris());
                builder.queryParam("uris", uris);
            }

            ResponseEntity<List<ViewStatDto>> response = restClient.get()
                    .uri(builder.build().toUri())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<>() {
                    });

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                log.error("Ошибка при получении статистики: {}", response.getStatusCode());
                return Collections.emptyList(); // Или выбросить исключение, в зависимости от требований
            }

        } catch (Exception e) {
            log.error("Ошибка при запросе статистики: {}", e.getMessage());
            return Collections.emptyList(); // Или выбросить исключение, в зависимости от требований
        }
    }
}