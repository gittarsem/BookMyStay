package com.tarsem.emailservice.consumer;

import com.tarsem.bookmystay.events.booking.BookingCancelledEvent;
import com.tarsem.bookmystay.events.booking.BookingConfirmedEvent;
import com.tarsem.bookmystay.events.booking.BookingExpiredEvent;
import com.tarsem.emailservice.constants.KafkaTopics;
import com.tarsem.emailservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
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
    public void consumeConfirmEvent(BookingConfirmedEvent event) {

        log.info(
                "Received BookingConfirmedEvent for booking {}",
                event.getBookingId()
        );

        emailService.sendBookingConfirmationEmail(event);
    }

    @KafkaListener(
            topics = KafkaTopics.BOOKING_CANCELLED,
            groupId = "email-service"
    )
    public void consumeCancelEvent(BookingCancelledEvent event) {

        log.info(
                "Received BookingCancelledEvent for booking {}",
                event.getBookingId()
        );

        emailService.sendBookingCancellationEmail(event);
    }

    @KafkaListener(
            topics = KafkaTopics.BOOKING_EXPIRED,
            groupId = "email-service"
    )
    public void consumeExpiredEvent(BookingExpiredEvent event) {

        log.info(
                "Received BookingExpiredEvent for booking {}",
                event.getBookingId()
        );

        emailService.sendBookingExpirationEmail(event);
    }

    @KafkaListener(
            topics = KafkaTopics.BOOKING_CONFIRMED_DLT,
            groupId = "email-service-dlt"
    )
    public void consumeDlt(BookingConfirmedEvent event) {

        log.error("""
                Booking moved to Dead Letter Topic

                Event Id   : {}
                Booking Id : {}
                Customer   : {}
                Email      : {}
                """,
                event.getEventId(),
                event.getBookingId(),
                event.getCustomerName(),
                event.getCustomerEmail());
    }

    @KafkaListener(
            topics = KafkaTopics.BOOKING_CANCELLED_DLT,
            groupId = "email-service-dlt"
    )
    public void consumeCancelledDlt(BookingCancelledEvent event) {

        log.error("""
                Booking cancellation moved to Dead Letter Topic

                Event Id   : {}
                Booking Id : {}
                Customer   : {}
                Email      : {}
                """,
                event.getEventId(),
                event.getBookingId(),
                event.getCustomerName(),
                event.getCustomerEmail());
    }

    @KafkaListener(
            topics = KafkaTopics.BOOKING_EXPIRED_DLT,
            groupId = "email-service-dlt"
    )
    public void consumeExpiredDlt(BookingExpiredEvent event) {

        log.error("""
                Booking Expired moved to Dead Letter Topic

                Event Id   : {}
                Booking Id : {}
                Customer   : {}
                Email      : {}
                """,
                event.getEventId(),
                event.getBookingId(),
                event.getCustomerName(),
                event.getCustomerEmail());
    }
}