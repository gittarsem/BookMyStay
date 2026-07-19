package com.tarsem.BookMyStay.dto.payment;

import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Enums.PaymentStatus;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyPaymentResponse {
    private Long bookingId;

    private String razorpayPaymentId;

    private PaymentStatus paymentStatus;

    private BookingStatus bookingStatus;

    private String message;
}
