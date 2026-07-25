package com.tarsem.emailservice.consumer;

import com.tarsem.emailservice.dto.BookingConfirmedEvent;
import com.tarsem.emailservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingEventConsumer {
    private EmailService emailService;

    public void consume(BookingConfirmedEvent event){
        emailService.sendBookingConfirmationEmail(event);
    }
}
