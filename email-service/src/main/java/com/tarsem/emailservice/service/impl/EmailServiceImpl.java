package com.tarsem.emailservice.service.impl;

import com.tarsem.bookmystay.events.booking.BookingConfirmedEvent;
import com.tarsem.emailservice.provider.SMTPEmailProvider;
import com.tarsem.emailservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final SMTPEmailProvider emailProvider;
    private final TemplateEngine templateEngine;

    public void sendBookingConfirmationEmail(BookingConfirmedEvent event) {
        log.info("Preparing booking confirmation email for bookingId={}", event.getBookingId());

        String emailId=event.getCustomerEmail();
        String subject = "Hey Traveler : Here is your room";

        String body = String.format("""
Hey %s! 👋

🎉 YOUR BOOKING IS LOCKED IN!

Great news! Your stay has been confirmed and you're all set. 🏨✨

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📋 BOOKING SUMMARY

🆔 Booking ID   : %d
🏨 Hotel        : %s
🛏️ Room Type    : %s
📅 Check-In     : %s
📅 Check-Out    : %s
💳 Amount Paid  : ₹%.2f

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

🚀 Before You Go

✅ Carry a valid government-issued ID
✅ Reach the hotel after the check-in time
✅ Keep this email handy during your trip

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

💙 Thanks for choosing BookMyStay.

We can't wait to host you. Have an amazing trip and make some unforgettable memories! 🌍✨

Need help?
Just reply to this email—we're always happy to help.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Cheers,
❤️ Team BookMyStay

Stay • Explore • Repeat ✈️
""",
                event.getCustomerName(),
                event.getBookingId(),
                event.getHotelName(),
                event.getRoomType(),
                event.getCheckInDate(),
                event.getCheckOutDate() != null ? event.getCheckOutDate() : "N/A",
                event.getAmountPaid()
        );
        try {
            emailProvider.sendEmail(
                    event.getCustomerEmail(),
                    subject,
                    body
            );

            log.info("Booking confirmation email sent for booking {}",
                    event.getBookingId());

        } catch (Exception ex) {

            log.error("Failed to send booking confirmation email for booking {}",
                    event.getBookingId(),
                    ex);

            throw ex;
        }


    }
}
