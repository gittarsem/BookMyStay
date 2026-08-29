package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Service.Interfaces.HotelService;
import com.tarsem.BookMyStay.Service.Interfaces.InventoryService;
import com.tarsem.BookMyStay.Service.Interfaces.RoomService;
import com.tarsem.BookMyStay.dto.hotel.HotelInfoDTO;
import com.tarsem.BookMyStay.dto.hotel.HotelResponseDTO;
import com.tarsem.BookMyStay.dto.hotel.HotelSearchResponseDTO;
import com.tarsem.BookMyStay.dto.hotel.RoomTypeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hotels")
@Tag(name = "Hotel Browse", description = "Browse and search for hotels")
public class HotelBrowseController {

    private final HotelService hotelService;
    private final RoomService roomService;
    private final InventoryService inventoryService;

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDTO> getHotelInfo(@PathVariable Long hotelId){
        return ResponseEntity.ok(hotelService.findHotelById(hotelId));
    }

    @GetMapping("/search")
    @Operation(summary = "Search for hotels", description = "Filter hotels based on location, price, availability, etc.")
    public ResponseEntity<HotelSearchResponseDTO> searchHotels(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double ratings,
            @RequestParam(required = false)
            LocalDate checkInDate,
            @RequestParam(required = false)
            LocalTime checkInTime,
            @RequestParam(required = false)
            LocalDate checkOutDate,
            @RequestParam(required = false)
            LocalTime checkOutTime,
            @RequestParam(defaultValue = "price") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws IOException {
        return ResponseEntity.ok(inventoryService.searchHotels(
                keyword,
                city,
                minPrice,
                maxPrice,
                ratings,
                checkInDate,
                checkInTime,
                checkOutDate,
                checkOutTime,
                sortField,
                sortOrder,
                page,
                size
        ));
    }

    @GetMapping
    @Operation(summary = "Get all hotels", description = "Fetch all hotels")
    public ResponseEntity<List<HotelResponseDTO>> getAllHotels(){
        return ResponseEntity.ok(hotelService.getAllHotel());
    }


    @GetMapping("/{hotelId}/rooms")
    public ResponseEntity<List<RoomTypeDTO>> getRoomTypes(
            @PathVariable Long hotelId) {

        return ResponseEntity.ok(
                roomService.getRoomTypes(hotelId)
        );
    }
}
