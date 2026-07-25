package com.tarsem.BookMyStay.dto.payment;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyPaymentRequest {

    private String orderId;

    private String paymentId;

    private String signature;

}
