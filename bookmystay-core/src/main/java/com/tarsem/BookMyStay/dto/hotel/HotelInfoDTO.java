package com.tarsem.BookMyStay.dto.hotel;

import com.tarsem.BookMyStay.Enums.HotelAmenity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HotelInfoDTO {

    private HotelResponseDTO hotels;

    private List<RoomDTO> rooms;

    private String description;

    private List<String> images;

    private List<HotelAmenity> amenities;

    private Double rating;

    private Integer reviewCount;

    private Double minPrice;

}
