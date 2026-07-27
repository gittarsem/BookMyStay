package com.tarsem.emailservice.service;

import com.tarsem.bookmystay.events.booking.BookingCancelledEvent;
import com.tarsem.bookmystay.events.booking.BookingConfirmedEvent;
import com.tarsem.bookmystay.events.booking.BookingExpiredEvent;

public interface EmailService {

    void sendBookingConfirmationEmail(BookingConfirmedEvent event);
    void sendBookingCancellationEmail(BookingCancelledEvent event);

    void sendBookingExpirationEmail(BookingExpiredEvent event);
}
