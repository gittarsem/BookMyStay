package com.tarsem.BookMyStay.dto;

import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Enums.PaymentStatus;
import com.tarsem.BookMyStay.Enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BookingHistoryDTO {
    Long bookingId;

    Long hotelId;

    String hotelName;

    String city;

    RoomType roomType;

    LocalDate checkInDate;

    LocalDate checkOutDate;

    BookingStatus bookingStatus;

    PaymentStatus paymentStatus;

    BigDecimal amount;
}
