package com.tarsem.bookmystay.events.booking;

import com.tarsem.bookmystay.events.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
public class BookingConfirmedEvent extends BaseEvent {
    private Long bookingId;

    private Long userId;

    private String customerName;

    private String customerEmail;

    private String hotelName;

    private String roomType;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private Integer adults;

    private Integer children;

    private BigDecimal amountPaid;

    private String paymentId;
}
