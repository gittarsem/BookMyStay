package com.tarsem.BookMyStay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelInfoDTO {
    private HotelResponseDTO hotel;
    private List<RoomDTO> rooms;
}
