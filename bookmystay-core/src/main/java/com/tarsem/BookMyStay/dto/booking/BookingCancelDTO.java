package com.tarsem.BookMyStay.dto.booking;

import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Enums.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

    private BigDecimal refundAmount;

    private String message;

    private RefundStatus refundStatus;
}