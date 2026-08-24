package com.tarsem.BookMyStay.Service.Interfaces;

import com.tarsem.BookMyStay.Enums.RoomType;
import com.tarsem.BookMyStay.Exceptions.UnAuthorisedException;
import com.tarsem.BookMyStay.dto.hotel.HotelPricingDTO;
import com.tarsem.BookMyStay.dto.hotel.HotelInfoDTO;
import com.tarsem.BookMyStay.dto.hotel.HotelRequestDTO;
import com.tarsem.BookMyStay.dto.hotel.HotelResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface HotelService {
    HotelResponseDTO createHotel(HotelRequestDTO hotelRequestDTO, List<MultipartFile> img) throws IOException;

    HotelResponseDTO getHotel(Long hotelId) throws UnAuthorisedException;

    HotelResponseDTO updateHotelById(HotelRequestDTO hotelRequestDTO, Long id);

    String deleteHotelById(Long hotelId);

    List<HotelResponseDTO> getAllHotel();

    String activateHotelById(Long hotelId);

    HotelInfoDTO findHotelById(Long hotelId);

    List<HotelResponseDTO> getMyHotels();

    List<HotelPricingDTO>  putHotelPricing(Long hotelId, RoomType roomType, HotelPricingDTO hotelPricingDTO);
}
