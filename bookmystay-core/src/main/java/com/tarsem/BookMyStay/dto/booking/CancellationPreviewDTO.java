package com.tarsem.BookMyStay.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancellationPreviewDTO {

    private Long bookingId;

    private BigDecimal amountPaid;

    private BigDecimal refundPercentage;

    private BigDecimal refundAmount;

    private BigDecimal cancellationFee;
}