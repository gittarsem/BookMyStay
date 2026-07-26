package com.tarsem.BookMyStay.producer;

import com.tarsem.BookMyStay.constants.KafkaTopics;
import com.tarsem.bookmystay.events.booking.BookingConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaProducerService {
    private final KafkaTemplate<String, BookingConfirmedEvent> template;

    public void publishConfirmedBooking(BookingConfirmedEvent event){
        log.info("Publishing BookingConfirmedEvent for bookingId={}", event.getBookingId());
        template.send(KafkaTopics.BOOKING_CONFIRMED,event);

    }
}
