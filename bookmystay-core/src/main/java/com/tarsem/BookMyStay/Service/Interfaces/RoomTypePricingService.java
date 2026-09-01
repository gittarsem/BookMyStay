package com.tarsem.BookMyStay.Service.Interfaces;

import com.tarsem.BookMyStay.dto.hotel.RoomTypeDTO;

import java.util.List;

public interface RoomTypePricingService {

    List<RoomTypeDTO> getAllPricing(Long hotelId);

    RoomTypeDTO getPricing(Long hotelId, Long pricingId);

    RoomTypeDTO createPricing(Long hotelId, RoomTypeDTO roomTypeDTO);

    RoomTypeDTO updatePricing(
            Long hotelId,
            Long pricingId,
            RoomTypeDTO roomTypeDTO
    );

    String deletePricing(Long hotelId, Long pricingId);
}