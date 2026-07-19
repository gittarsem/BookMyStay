package com.tarsem.BookMyStay.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class CreateOrderResponse {

    private String orderId;

    private BigDecimal amount;

    private String currency;

    private String keyId;
}
