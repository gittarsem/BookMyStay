package com.tarsem.BookMyStay.Service.Interfaces;

import com.tarsem.BookMyStay.dto.HotelInfoDTO;
import com.tarsem.BookMyStay.dto.HotelRequestDTO;
import com.tarsem.BookMyStay.dto.HotelResponseDTO;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface HotelService {
    HotelResponseDTO createHotel(HotelRequestDTO hotelRequestDTO);

    HotelResponseDTO getHotel(Long hotelId);

    HotelResponseDTO updateHotelById(HotelRequestDTO hotelRequestDTO, Long id);

    String deleteHotelById(Long hotelId);

    List<HotelResponseDTO> getAllHotel();

    String activateHotelById(Long hotelId);

    HotelInfoDTO findHotelById(Long hotelId);
}
