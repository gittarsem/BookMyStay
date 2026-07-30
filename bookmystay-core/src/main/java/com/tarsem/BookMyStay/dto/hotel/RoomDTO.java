package com.tarsem.BookMyStay.dto.hotel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomDTO {
    private int capacity;
    private String roomType;
    private Double price;
}
