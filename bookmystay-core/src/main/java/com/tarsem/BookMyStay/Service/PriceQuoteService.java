package com.tarsem.BookMyStay.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarsem.BookMyStay.Exceptions.BusinessRuleViolationException;
import com.tarsem.BookMyStay.dto.booking.PriceQuoteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PriceQuoteService {

    private static final String QUOTE_PREFIX = "price_quote:";

    private static final Duration QUOTE_TTL =
            Duration.ofMinutes(10);

    private static final Duration CLAIM_TTL =
            Duration.ofMinutes(2);

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;


    /*
     * Atomically claims a quote.
     *
     * If the quote is already claimed, another request
     * cannot claim it until the claim expires.
     */
    private static final DefaultRedisScript<String> CLAIM_QUOTE =
            new DefaultRedisScript<>(
                    """
                    local value = redis.call('GET', KEYS[1])

                    if not value then
                        return nil
                    end

                    local claimedKey = KEYS[1] .. ':claimed'

                    if redis.call('EXISTS', claimedKey) == 1 then
                        return nil
                    end

                    redis.call(
                        'SET',
                        claimedKey,
                        '1',
                        'EX',
                        ARGV[1]
                    )

                    return value
                    """,
                    String.class
            );


    /*
     * Create and store a new quote.
     */
    public PriceQuoteDTO createQuote(
            PriceQuoteDTO quote
    ) {

        String quoteId =
                UUID.randomUUID().toString();

        quote.setQuoteId(quoteId);

        String key =
                QUOTE_PREFIX + quoteId;

        try {

            String value =
                    objectMapper.writeValueAsString(quote);

            redisTemplate.opsForValue().set(
                    key,
                    value,
                    QUOTE_TTL
            );

            return quote;

        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                    "Unable to create price quote.",
                    e
            );
        }
    }


    /*
     * Atomically claim a quote.
     *
     * Only one request can successfully claim
     * the same quote at a time.
     */
    public PriceQuoteDTO claimQuote(
            String quoteId
    ) {

        if (quoteId == null || quoteId.isBlank()) {

            throw new BusinessRuleViolationException(
                    "Quote ID is required."
            );
        }

        String key =
                QUOTE_PREFIX + quoteId;

        String value =
                redisTemplate.execute(
                        CLAIM_QUOTE,
                        List.of(key),
                        String.valueOf(
                                CLAIM_TTL.toSeconds()
                        )
                );

        if (value == null) {

            throw new BusinessRuleViolationException(
                    "Price quote has expired or is already being used."
            );
        }

        try {

            return objectMapper.readValue(
                    value,
                    PriceQuoteDTO.class
            );

        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                    "Unable to read price quote.",
                    e
            );
        }
    }


    /*
     * Called after the booking transaction commits.
     */
    public void completeQuote(
            String quoteId
    ) {

        redisTemplate.delete(
                QUOTE_PREFIX + quoteId
        );

        redisTemplate.delete(
                QUOTE_PREFIX + quoteId + ":claimed"
        );
    }


    /*
     * Called when the booking transaction fails.
     * Allows the quote to be used again.
     */
    public void releaseQuote(
            String quoteId
    ) {

        redisTemplate.delete(
                QUOTE_PREFIX + quoteId + ":claimed"
        );
    }
}