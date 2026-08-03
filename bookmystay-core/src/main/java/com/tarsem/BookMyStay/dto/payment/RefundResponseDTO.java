package com.tarsem.BookMyStay.dto.payment;

import com.tarsem.BookMyStay.Enums.RefundStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class RefundResponseDTO {

    private Long bookingId;

    private String refundId;

    private Double refundAmount;

    private RefundStatus refundStatus;

    private String message;
}
