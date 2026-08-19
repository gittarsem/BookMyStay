package com.tarsem.BookMyStay.dto.booking;

import com.tarsem.BookMyStay.Enums.BookingMode;
import com.tarsem.BookMyStay.Enums.RoomType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class BookingRequestDTO {
    private long hotelId;
    private String quoteId;
    private RoomType roomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int adultCount;
    private int childCount;
    private BookingMode bookingMode;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
}
