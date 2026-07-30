package com.tarsem.BookMyStay.dto.booking;

import com.tarsem.BookMyStay.Enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCancelDTO {

    private Long bookingId;

    private BookingStatus bookingStatus;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private String message;
}