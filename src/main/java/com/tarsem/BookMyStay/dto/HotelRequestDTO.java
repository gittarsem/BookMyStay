package com.tarsem.BookMyStay.dto;

import com.tarsem.BookMyStay.Entity.HotelContactInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HotelRequestDTO {
    private String name;
    private HotelContactInfo hotelContactInfo;
}
