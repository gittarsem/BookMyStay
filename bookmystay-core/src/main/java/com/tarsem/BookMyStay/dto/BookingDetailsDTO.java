package com.tarsem.BookMyStay.dto;

import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Enums.PaymentStatus;
import com.tarsem.BookMyStay.Enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDetailsDTO {
    Long bookingId;

    String hotelName;
    String city;

    RoomType roomType;

    LocalDate checkInDate;
    LocalDate checkOutDate;

    Integer adultCount;
    Integer childCount;

    BookingStatus bookingStatus;
    PaymentStatus paymentStatus;

    BigDecimal amount;

    Set<GuestDTO> guests;
}
