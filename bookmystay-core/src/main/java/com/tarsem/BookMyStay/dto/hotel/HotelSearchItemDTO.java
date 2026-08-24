package com.tarsem.BookMyStay.dto.hotel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HotelSearchItemDTO {

    private Long id;

    private String name;

    private String city;

    private Double price;

    private Double rating;

    private Boolean active;

    private List<String> images;
}
