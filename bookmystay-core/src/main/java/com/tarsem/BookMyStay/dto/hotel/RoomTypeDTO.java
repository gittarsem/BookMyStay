package com.tarsem.BookMyStay.dto.hotel;

import com.tarsem.BookMyStay.Enums.RoomType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomTypeDTO {

    private RoomType roomType;
    private BigDecimal price;
    private int capacity;
    private int totalRooms;
}
