package com.tarsem.BookMyStay.dto;

import com.tarsem.BookMyStay.Entity.HotelContactInfo;
import lombok.Data;

@Data
public class HotelResponseDTO {
    private Long id;
    private String name;
    private HotelContactInfo hotelContactInfo;
    private boolean active;
}
