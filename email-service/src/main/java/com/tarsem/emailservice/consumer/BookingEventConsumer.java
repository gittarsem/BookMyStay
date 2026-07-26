package com.tarsem.emailservice.consumer;

import com.tarsem.bookmystay.events.booking.BookingConfirmedEvent;
import com.tarsem.emailservice.constants.KafkaTopics;
import com.tarsem.emailservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventConsumer {
    private final EmailService emailService;


    @KafkaListener(
            topics = KafkaTopics.BOOKING_CONFIRMED,
            groupId = "email-service"
    )
    public void consume(BookingConfirmedEvent event) {
        log.info("Received BookingConfirmedEvent for booking {}",
                event.getBookingId());
        emailService.sendBookingConfirmationEmail(event);
    }
}
