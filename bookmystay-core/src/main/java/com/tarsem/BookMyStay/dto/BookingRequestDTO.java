package com.tarsem.BookMyStay.dto;

import com.tarsem.BookMyStay.Enums.RoomType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequestDTO {
    private long hotelId;
    private RoomType roomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int adultCount;
    private int childCount;
}
