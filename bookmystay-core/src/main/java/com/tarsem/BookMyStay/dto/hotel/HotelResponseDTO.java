package com.tarsem.BookMyStay.dto.hotel;

import com.tarsem.BookMyStay.Entity.HotelContactInfo;
import lombok.Data;

@Data
public class HotelResponseDTO {
    private Long id;
    private String name;
    private String city;
    private HotelContactInfo hotelContactInfo;
    private boolean active;
}
