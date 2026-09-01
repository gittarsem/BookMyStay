package com.tarsem.BookMyStay.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelInventoryDTO {

    private LocalDate date;

    private List<RoomTypeInventoryDTO> roomTypes;
}