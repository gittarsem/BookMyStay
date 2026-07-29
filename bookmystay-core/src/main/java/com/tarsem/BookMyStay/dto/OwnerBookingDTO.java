package com.tarsem.BookMyStay.dto;

import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Enums.PaymentStatus;
import com.tarsem.BookMyStay.Enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerBookingDTO {
    Long bookingId;

    String guestName;

    RoomType roomType;

    LocalDate checkInDate;

    LocalDate checkOutDate;

    BookingStatus bookingStatus;

    PaymentStatus paymentStatus;

    BigDecimal amount;
}
