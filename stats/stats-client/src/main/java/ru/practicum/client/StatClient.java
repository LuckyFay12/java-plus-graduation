package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.StatRequest;
import ru.practicum.ewm.dto.ViewStatDto;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class StatClient {

    private final RestClient restClient;
    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${stats-server.name:stats-server}")  //в main-service
    private String statsServiceId;

    public StatClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
        this.restClient = RestClient.create();
        this.retryTemplate = createRetryTemplate();
    }

    private RetryTemplate createRetryTemplate() {
        RetryTemplate template = new RetryTemplate();
        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy(3);
        template.setRetryPolicy(retryPolicy);

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(3000L);
        template.setBackOffPolicy(backOffPolicy);

        return template;
    }

    private ServiceInstance getStatsServerInstance() {
        return retryTemplate.execute(context -> {
            log.debug("Попытка #{} получить экземпляр сервиса {}",
                    context.getRetryCount() + 1, statsServiceId);

            List<ServiceInstance> instances = discoveryClient.getInstances(statsServiceId);
            if (instances == null || instances.isEmpty()) {
                throw new IllegalStateException(
                        "Сервис статистики '" + statsServiceId + "' не найден в Eureka"
                );
            }

            ServiceInstance instance = instances.get(0);
            log.debug("Найден экземпляр: {}:{}", instance.getHost(), instance.getPort());
            return instance;
        });
    }

    private URI buildStatsServiceUri(String path) {
        ServiceInstance instance = getStatsServerInstance();
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(instance.getHost())
                .port(instance.getPort())
                .path(path)
                .build()
                .toUri();
    }

    public ResponseEntity<Object> saveHit(EndpointHit hit) {
        try {
            log.info("Сохранение информации о запросе {}", hit);
            URI uri = buildStatsServiceUri("/hit");

            return restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(hit)
                    .retrieve()
                    .toEntity(Object.class);

        } catch (Exception e) {
            log.error("Ошибка при сохранении информации: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    public List<ViewStatDto> getStats(StatRequest request) {
        if (request == null || !request.isValid()) {
            log.warn("Некорректные параметры запроса статистики");
            return Collections.emptyList();
        }

        try {
            log.info("Запрос статистики {}", request);
            URI baseUri = buildStatsServiceUri("/stats");

            UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseUri)
                    .queryParam("start", request.getStart().format(FORMATTER))
                    .queryParam("end", request.getEnd().format(FORMATTER))
                    .queryParam("unique", request.getUnique());

            if (request.getUris() != null && !request.getUris().isEmpty()) {
                String uris = String.join(",", request.getUris());
                builder.queryParam("uris", uris);
            }

            URI finalUri = builder.build().toUri();

            ResponseEntity<List<ViewStatDto>> response = restClient.get()
                    .uri(finalUri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<>() {
                    });

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                log.error("Ошибка при получении статистики: {}", response.getStatusCode());
                return Collections.emptyList();
            }

        } catch (Exception e) {
            log.error("Ошибка при запросе статистики: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}