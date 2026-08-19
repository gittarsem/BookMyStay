package com.tarsem.BookMyStay.dto.booking;

import com.tarsem.BookMyStay.Enums.BookingMode;
import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Enums.PaymentStatus;
import com.tarsem.BookMyStay.Enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

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

    BookingMode bookingMode;

    LocalDate checkInDate;

    LocalDate checkOutDate;

    LocalTime checkInTime;

    LocalTime checkOutTime;

    int adultCount;

    int childCount;

    BookingStatus bookingStatus;

    PaymentStatus paymentStatus;

    BigDecimal amount;

    String hotelImage;

    Long reviewId;
}