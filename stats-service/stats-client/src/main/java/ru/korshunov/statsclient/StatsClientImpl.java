package ru.korshunov.statsclient;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import statsdto.HitDto;
import statsdto.StatDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StatsClientImpl implements StatsClient {

    private final RestClient restClient;
    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;
    private final String statsServiceId;
    private static final String PATH_HIT = "/hit";
    private static final String PATH_STATS = "/stats";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClientImpl(@Value("${stats-client.service-id:stats-server}") String statsServiceId,
                           RestClient.Builder restClientBuilder,
                           DiscoveryClient discoveryClient) {
        this.restClient = restClientBuilder.build();
        this.discoveryClient = discoveryClient;
        this.statsServiceId = statsServiceId;
        this.retryTemplate = createRetryTemplate();
    }

    @Override
    public void addHit(@Valid HitDto hitDto) {
        String uri = UriComponentsBuilder
                .fromPath(PATH_HIT)
                .build()
                .toUriString();

        post(uri, hitDto);
    }

    private ResponseEntity<Void> post(String uri, Object body) {
        return restClient
                .post()
                .uri(makeUri(uri))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public List<StatDto> getStats(LocalDateTime start, LocalDateTime end) {
        String uri = UriComponentsBuilder
                .fromPath(PATH_STATS)
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .build()
                .encode()
                .toUriString();

        return get(uri);
    }

    private List<StatDto> get(String uri) {
        return restClient
                .get()
                .uri(makeUri(uri))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<StatDto>>() {
                });
    }

    @Override
    public List<StatDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris) {
        String uri = UriComponentsBuilder
                .fromPath(PATH_STATS)
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("uris", uris.toArray())
                .build()
                .encode()
                .toUriString();

        return get(uri);
    }

    @Override
    public List<StatDto> getStats(LocalDateTime start, LocalDateTime end, Boolean unique) {
        String uri = UriComponentsBuilder
                .fromPath(PATH_STATS)
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("unique", unique)
                .build()
                .encode()
                .toUriString();

        return get(uri);
    }

    @Override
    public List<StatDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        String uri = UriComponentsBuilder
                .fromPath(PATH_STATS)
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("uris", uris.toArray())
                .queryParam("unique", unique)
                .build()
                .encode()
                .toUriString();

        return get(uri);
    }

    private RetryTemplate createRetryTemplate() {
        RetryTemplate template = new RetryTemplate();

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(3000L);
        template.setBackOffPolicy(backOffPolicy);

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        template.setRetryPolicy(retryPolicy);

        return template;
    }

    private ServiceInstance getInstance() {
        return discoveryClient.getInstances(statsServiceId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new StatsServerUnavailable(
                        "Ошибка обнаружения адреса сервиса статистики с id: " + statsServiceId
                ));
    }

    private String makeUri(String path) {
        ServiceInstance instance = retryTemplate.execute(context -> getInstance());
        return "http://" + instance.getHost() + ":" + instance.getPort() + path;
    }
}
