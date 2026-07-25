package com.tarsem.emailservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingConfirmedEvent {

    private Long bookingId;

    private Long userId;

    private String name;

    private String email;

    private String hotelName;

    private String RoomType;

    private LocalDateTime checkInDate;

    private LocalDateTime checkOutDate;

    private BigDecimal amount;

    private LocalDateTime bookingTime;
}
