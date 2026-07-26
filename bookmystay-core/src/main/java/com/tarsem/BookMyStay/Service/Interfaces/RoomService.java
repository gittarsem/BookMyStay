package com.tarsem.BookMyStay.Service.Interfaces;

import com.tarsem.BookMyStay.dto.RoomDTO;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface RoomService {
    RoomDTO addNewRoom(RoomDTO roomDTO, Long hotelId);
    List<RoomDTO> giveAllRoomsInHotel(Long hotelId);
    RoomDTO getRoomById(Long hotelId, Long roomId);
    RoomDTO updateRoomById(Long hotelId, Long roomId, RoomDTO roomDTO);

    String deleteRoomById(Long hotelId,Long roomId);
}
