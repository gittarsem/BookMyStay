package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Service.Interfaces.HotelService;
import com.tarsem.BookMyStay.Service.Interfaces.InventoryService;
import com.tarsem.BookMyStay.dto.HotelInfoDTO;
import com.tarsem.BookMyStay.dto.HotelPriceDTO;
import com.tarsem.BookMyStay.dto.HotelSearchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hotels")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Hotel Browse", description = "Browse and search for hotels")
public class HotelBrowseController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDTO> getHotelInfo(@PathVariable Long hotelId){
        return ResponseEntity.ok(hotelService.findHotelById(hotelId));
    }

    @GetMapping("/search")
    @Operation(summary = "Search for hotels", description = "Filter hotels based on location, price, availability, etc.")
    public ResponseEntity<Page<HotelPriceDTO>> searchHotels(@RequestBody HotelSearchRequest hotelSearchRequest) {
        Page<HotelPriceDTO> page = inventoryService.searchHotels(hotelSearchRequest);
        return ResponseEntity.ok(page);
    }

}
