package com.tarsem.emailservice.service.impl;

import com.tarsem.emailservice.dto.BookingConfirmedEvent;
import com.tarsem.emailservice.provider.SMTPEmailProvider;
import com.tarsem.emailservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl {

    private final SMTPEmailProvider emailProvider;

    public void sendBookingConfirmationEmail(BookingConfirmedEvent event) {
        log.info("Preparing booking confirmation email for bookingId={}", event.getBookingId());

        String emailId=event.getEmail();
        String subject = "Booking Confirmed - BookMyStay";

        String body = String.format(
                """
                        Dear %s,
                
                        We're delighted to inform you that your booking has been confirmed.
                
                        Booking Details
                        ----------------------------
                        Booking ID : %d
                        Hotel      : %s
                        Room Type  : %s
                        Check-In   : %s
                        Check-Out  : %s
                        Amount Paid: ₹%s
                        ----------------------------
                
                        We look forward to welcoming you and hope you have a pleasant stay.
                
                        If you have any questions, feel free to contact our support team.
                
                        Thank you for choosing BookMyStay.
                
                        Best Regards,
                        BookMyStay Team
                """,
                event.getName(),
                event.getBookingId(),
                event.getHotelName(),
                event.getRoomType(),
                event.getCheckInDate().toLocalDate(),
                event.getCheckOutDate().toLocalDate(),
                event.getAmount()

        );

        emailProvider.sendEmail(
                emailId,
                subject,
                body
        );

        log.info("Booking confirmation email request forwarded to SMTP provider for bookingId={}",
                event.getBookingId());


    }
}
