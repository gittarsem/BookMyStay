package com.tarsem.BookMyStay.dto;

import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Enums.PaymentStatus;
import com.tarsem.BookMyStay.Enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerBookingDetailsDTO {
    Long bookingId;

    String guestName;

    String email;

    String phone;

    String hotelName;

    String city;

    RoomType roomType;

    Integer adultCount;

    Integer childCount;

    LocalDate checkInDate;

    LocalDate checkOutDate;

    BookingStatus bookingStatus;

    PaymentStatus paymentStatus;

    BigDecimal amount;

    Set<GuestDTO> guests;
}
