package com.tarsem.BookMyStay.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceQuoteDTO {

    private String quoteId;

    private Long hotelId;

    private Long roomId;

    private BigDecimal finalPrice;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    private String roomType;

    private String bookingMode;
}