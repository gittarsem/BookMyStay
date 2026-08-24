package com.tarsem.BookMyStay.dto.hotel;

import com.tarsem.BookMyStay.Enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomDTO {
    private int capacity;
    private RoomType roomType;
    private Double price;
}
