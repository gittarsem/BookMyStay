package com.tarsem.BookMyStay.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequestDTO {
    private long hotelId;
    private long roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer roomsCount;
}
