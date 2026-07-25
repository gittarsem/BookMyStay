package com.tarsem.emailservice.service;

import com.tarsem.emailservice.dto.BookingConfirmedEvent;

public interface EmailService {

    void sendBookingConfirmationEmail(BookingConfirmedEvent event);
}
