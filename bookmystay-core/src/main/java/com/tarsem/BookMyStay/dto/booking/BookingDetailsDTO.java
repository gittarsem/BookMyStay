package com.tarsem.BookMyStay.dto.booking;

import com.tarsem.BookMyStay.Enums.BookingMode;
import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Enums.PaymentStatus;
import com.tarsem.BookMyStay.Enums.RoomType;
import com.tarsem.BookMyStay.dto.hotel.GuestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDetailsDTO {

    Long bookingId;

    String hotelName;

    String city;

    RoomType roomType;

    BookingMode bookingMode;

    LocalDate checkInDate;

    LocalDate checkOutDate;

    LocalTime checkInTime;

    LocalTime checkOutTime;

    Integer adultCount;

    Integer childCount;

    BookingStatus bookingStatus;

    PaymentStatus paymentStatus;

    BigDecimal amount;

    Set<GuestDTO> guests;
}