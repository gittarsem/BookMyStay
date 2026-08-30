package com.tarsem.emailservice.service.impl;

import com.tarsem.bookmystay.events.booking.BookingCancelledEvent;
import com.tarsem.bookmystay.events.booking.BookingConfirmedEvent;
import com.tarsem.bookmystay.events.booking.BookingExpiredEvent;
import com.tarsem.emailservice.provider.SMTPEmailProvider;
import com.tarsem.emailservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final SMTPEmailProvider emailProvider;
    private final SpringTemplateEngine templateEngine;

    @Override
    public void sendBookingConfirmationEmail(
            BookingConfirmedEvent event
    ) {

        log.info(
                "Preparing booking confirmation email for bookingId={}",
                event.getBookingId()
        );

        Context context = new Context();

        context.setVariable("customerName", event.getCustomerName());
        context.setVariable("bookingId", event.getBookingId());
        context.setVariable("hotelName", event.getHotelName());
        context.setVariable("roomType", event.getRoomType());
        context.setVariable("checkInDate", event.getCheckInDate());
        context.setVariable(
                "checkOutDate",
                event.getCheckOutDate() != null
                        ? event.getCheckOutDate()
                        : "N/A"
        );
        context.setVariable("amountPaid", event.getAmountPaid());

        String body = templateEngine.process(
                "email/booking-confirmed",
                context
        );

        emailProvider.sendEmail(
                event.getCustomerEmail(),
                "Booking Confirmed | BookMyStay",
                body
        );

        log.info(
                "Booking confirmation email sent successfully for bookingId={}",
                event.getBookingId()
        );
    }

    @Override
    public void sendBookingCancellationEmail(
            BookingCancelledEvent event
    ) {

        log.info(
                "Preparing booking cancellation email for bookingId={}",
                event.getBookingId()
        );

        Context context = new Context();

        context.setVariable("customerName", event.getCustomerName());
        context.setVariable("bookingId", event.getBookingId());
        context.setVariable("hotelName", event.getHotelName());
        context.setVariable("roomType", event.getRoomType());
        context.setVariable("refundAmount", event.getRefundAmount());

        String body = templateEngine.process(
                "email/booking-cancelled",
                context
        );

        emailProvider.sendEmail(
                event.getCustomerEmail(),
                "Booking Cancelled | BookMyStay",
                body
        );

        log.info(
                "Booking cancellation email sent successfully for bookingId={}",
                event.getBookingId()
        );
    }

    @Override
    public void sendBookingExpirationEmail(
            BookingExpiredEvent event
    ) {

        log.info(
                "Preparing booking expiration email for bookingId={}",
                event.getBookingId()
        );

        Context context = new Context();

        context.setVariable("customerName", event.getCustomerName());
        context.setVariable("bookingId", event.getBookingId());
        context.setVariable("hotelName", event.getHotelName());
        context.setVariable("amountPaid", event.getAmountPaid());

        String body = templateEngine.process(
                "email/booking-expired",
                context
        );

        emailProvider.sendEmail(
                event.getCustomerEmail(),
                "Booking Expired | BookMyStay",
                body
        );

        log.info(
                "Booking expiration email sent successfully for bookingId={}",
                event.getBookingId()
        );
    }
}