package com.tarsem.emailservice.service.impl;

import com.tarsem.bookmystay.events.booking.BookingCancelledEvent;
import com.tarsem.bookmystay.events.booking.BookingConfirmedEvent;
import com.tarsem.bookmystay.events.booking.BookingExpiredEvent;
import com.tarsem.emailservice.provider.SMTPEmailProvider;
import com.tarsem.emailservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final SMTPEmailProvider emailProvider;

    public void sendBookingConfirmationEmail(BookingConfirmedEvent event) {
        log.info("Preparing booking confirmation email for bookingId={}", event.getBookingId());

        String subject = "Hey Traveler : Here is your room";

        String body = String.format("""
Hey %s!

YOUR BOOKING IS LOCKED IN!
Great news! Your stay has been confirmed and you're all set. 🏨✨

📋 BOOKING SUMMARY
━━━━━━━━━━━━━━━━━━━━━━━

Booking ID   : %d
Hotel        : %s
Room Type    : %s
Check-In     : %s
Check-Out    : %s
Amount Paid  : ₹%.2f
━━━━━━━━━━━━━━━━━━━━━━━

🚀 Before You Go

✅ Carry a valid government-issued ID
✅ Reach the hotel after the check-in time
✅ Keep this email handy during your trip

💙 Thanks for choosing BookMyStay.

We can't wait to host you. Have an amazing trip and make some unforgettable memories! 🌍✨

Need help?
Just reply to this email—we're always happy to help.

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

        emailProvider.sendEmail(
                event.getCustomerEmail(),
                subject,
                body
        );

        log.info("Booking confirmation email sent successfully for bookingId={}",
                event.getBookingId());

    }

    @Override
    public void sendBookingCancellationEmail(BookingCancelledEvent event) {
        log.info("Preparing booking cancellation email for bookingId={}", event.getBookingId());

        String subject = "Booking Cancelled | BookMyStay";

        String body = String.format("""
Hey %s! 👋

YOUR BOOKING HAS BEEN CANCELLED

We're sorry to let you know that your booking has been cancelled.

━━━━━━━━━━━━━━━━━━━━━━━

📋 CANCELLATION DETAILS

🆔 Booking ID   : %d
🏨 Hotel        : %s
🛏️ Room Type    : %s
💰 Refund Amount: ₹%.2f
━━━━━━━━━━━━━━━━━━━━━━━

ℹ️ What Happens Next

✅ If you're eligible for a refund, it will be processed to your original payment method.
✅ Refunds may take 5–7 business days depending on your bank.
✅ You can make a new booking anytime on BookMyStay.


💙 Thank you for choosing BookMyStay.

We hope to welcome you on your next trip. Safe travels, and we look forward to serving you again! 🌍✨

Need help?
Just reply to this email—we're always happy to help.

Cheers,
❤️ Team BookMyStay

Stay • Explore • Repeat ✈️
""",
                event.getCustomerName(),
                event.getBookingId(),
                event.getHotelName(),
                event.getRoomType(),
                event.getRefundAmount()

        );

        emailProvider.sendEmail(
                event.getCustomerEmail(),
                subject,
                body
        );

        log.info("Booking cancellation email sent successfully for bookingId={}",
                event.getBookingId());

    }

    @Override
    public void sendBookingExpirationEmail(BookingExpiredEvent event) {
        log.info("Preparing booking expiration email for bookingId={}", event.getBookingId());

        String subject = "Booking Expired | BookMyStay";
        String body = String.format("""
Hey %s! 👋

YOUR BOOKING HAS EXPIRED ⏰

Unfortunately, we couldn't confirm your booking because the payment window expired before the payment was completed.

━━━━━━━━━━━━━━━━━━━━━━━

📋 BOOKING DETAILS

🆔 Booking ID   : %d
🏨 Hotel        : %s
💰 Amount       : ₹%.2f
━━━━━━━━━━━━━━━━━━━━━━━

ℹ️ What Happened?

⏳ Every booking is reserved for a limited time to ensure fair availability.
❌ Since the payment wasn't completed within the allowed time, your booking has been automatically cancelled.

What's Next?
You can book the same hotel again at any time (subject to availability).
If your payment was deducted unexpectedly, please contact our support team and we'll help resolve it promptly.

💙 Thank you for choosing BookMyStay.

We hope to help you plan your next stay soon. 🌍✨

Need help?
Just reply to this email—we're always happy to help.

Cheers,
❤️ Team BookMyStay

Stay • Explore • Repeat ✈️
""",
                event.getCustomerName(),
                event.getBookingId(),
                event.getHotelName(),
                event.getAmountPaid()
        );

        emailProvider.sendEmail(
                event.getCustomerEmail(),
                subject,
                body
        );

        log.info("Booking expiration email sent successfully for bookingId={}",
                event.getBookingId());

    }
}
