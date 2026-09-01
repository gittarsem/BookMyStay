package com.tarsem.BookMyStay.dto.hotel;

import com.tarsem.BookMyStay.Enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomTypeDTO {

    private RoomType roomType;
    private BigDecimal hourlyPrice;
    private BigDecimal dailyPrice;
    private int capacity;
    private int totalRooms;
}