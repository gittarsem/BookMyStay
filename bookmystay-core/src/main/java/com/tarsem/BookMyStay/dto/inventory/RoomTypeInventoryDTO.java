package com.tarsem.BookMyStay.dto.inventory;

import com.tarsem.BookMyStay.Enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomTypeInventoryDTO {

    private RoomType roomType;

    private Integer totalRooms;

    private Integer bookedRooms;

    private Integer reservedRooms;

    private Integer availableRooms;

    private Boolean closed;

    private BigDecimal price;

    private BigDecimal surgeFactor;
}