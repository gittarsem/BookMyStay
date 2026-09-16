package com.tarsem.BookMyStay.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchAvailabilityService {

    private static final String ES_AVAILABLE_KEY =
            "elasticsearch:available";

    private static final Duration STATUS_TTL =
            Duration.ofMinutes(1);

    private final StringRedisTemplate redisTemplate;


    /**
     * Returns:
     *
     * true  -> Elasticsearch is known to be available
     * false -> Elasticsearch is known to be unavailable
     * null  -> Elasticsearch status is unknown
     */
    public Boolean getStatus() {

        String value =
                redisTemplate
                        .opsForValue()
                        .get(ES_AVAILABLE_KEY);

        if (value == null) {
            return null;
        }

        return Boolean.parseBoolean(value);
    }


    public void markAvailable() {

        redisTemplate
                .opsForValue()
                .set(
                        ES_AVAILABLE_KEY,
                        "true",
                        STATUS_TTL
                );

        log.debug("Elasticsearch marked as available");
    }


    public void markUnavailable() {

        redisTemplate
                .opsForValue()
                .set(
                        ES_AVAILABLE_KEY,
                        "false",
                        STATUS_TTL
                );

        log.warn(
                "Elasticsearch marked as unavailable for {} seconds",
                STATUS_TTL.toSeconds()
        );
    }
}