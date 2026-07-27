package com.tarsem.BookMyStay.producer;

import com.tarsem.BookMyStay.constants.KafkaTopics;
import com.tarsem.bookmystay.events.booking.BookingCancelledEvent;
import com.tarsem.bookmystay.events.booking.BookingConfirmedEvent;
import com.tarsem.bookmystay.events.events.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaProducerService {
    private final KafkaTemplate<String, BaseEvent> template;

    public void publishConfirmedBooking(BookingConfirmedEvent event){
        log.info("Publishing BookingConfirmedEvent for bookingId={}", event.getBookingId());
       template.send(KafkaTopics.BOOKING_CONFIRMED,event);

    }

    public void publishCancelledBooking(BookingCancelledEvent event){
        log.info("Publishing BookingCancelEvent for bookingId={}", event.getBookingId());
        template.send(KafkaTopics.BOOKING_CANCELLED,event);
    }

    public void publishExpiredBooking(BookingCancelledEvent event){
        log.info("Publishing BookingExpiredEvent for BookingId={} ",event.getBookingId());
        template.send(KafkaTopics.BOOKING_EXPIRED,event);
    }
}
