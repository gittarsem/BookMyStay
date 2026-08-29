package com.tarsem.BookMyStay.dto.hotel;

import com.tarsem.BookMyStay.Enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelPricingDTO {
    private RoomType roomType;
    private BigDecimal hourlyPrice;
    private BigDecimal dailyPrice;
}
