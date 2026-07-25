package com.tarsem.BookMyStay.dto;

import com.tarsem.BookMyStay.document.HotelDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelSearchResponseDTO {
    private List<HotelDocument> hotel;
    long total;
    int page;
    int size;
}
