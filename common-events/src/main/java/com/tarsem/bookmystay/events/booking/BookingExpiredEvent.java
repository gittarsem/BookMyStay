package com.tarsem.bookmystay.events.booking;

import com.tarsem.bookmystay.events.events.BaseEvent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
public class BookingExpiredEvent extends BaseEvent {
    private Long bookingId;

    private Long userId;

    private String customerName;

    private String customerEmail;

    private String hotelName;

    private BigDecimal amountPaid;
}
