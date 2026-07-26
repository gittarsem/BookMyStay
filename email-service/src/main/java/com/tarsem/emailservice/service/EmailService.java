package com.tarsem.emailservice.service;

import com.tarsem.bookmystay.events.booking.BookingConfirmedEvent;

public interface EmailService {

    void sendBookingConfirmationEmail(BookingConfirmedEvent event);
}
