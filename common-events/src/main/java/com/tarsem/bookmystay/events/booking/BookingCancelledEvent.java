package com.tarsem.bookmystay.events.booking;

import com.tarsem.bookmystay.events.events.BaseEvent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
public class BookingCancelledEvent extends BaseEvent {

    private Long bookingId;

    private Long userId;

    private String customerName;

    private String customerEmail;

    private String hotelName;

    private String roomType;

    private BigDecimal refundAmount;

}
