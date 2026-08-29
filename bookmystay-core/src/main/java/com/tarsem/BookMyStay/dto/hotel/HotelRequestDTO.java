package com.tarsem.BookMyStay.dto.hotel;

import com.tarsem.BookMyStay.Entity.HotelContactInfo;
import com.tarsem.BookMyStay.Enums.HotelAmenity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class HotelRequestDTO {
    private String name;

    private String city;

    private HotelContactInfo hotelContactInfo;

    private String description;

    private List<HotelAmenity> amenities;
}
