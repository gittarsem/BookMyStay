package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Service.Interfaces.HotelService;
import com.tarsem.BookMyStay.dto.HotelRequestDTO;
import com.tarsem.BookMyStay.dto.HotelResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@SecurityRequirement(name = "BearerAuth")
@RequestMapping("/admin/hotel")
@Tag(name = "Hotel Management", description = "Manage hotel details")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @PostMapping
    @Operation(summary = "Create a new hotel", description = "Adds a new hotel to the system")
    public ResponseEntity<HotelResponseDTO> createHotel(@RequestBody HotelRequestDTO hotelRequestDTO){
        log.info("Attempt to create new hotel with name: "+ hotelRequestDTO.getName());
        HotelResponseDTO hotel=hotelService.createHotel(hotelRequestDTO);
        return new ResponseEntity<>(hotel, HttpStatus.CREATED);
    }

    @GetMapping("/{hotelId}")
    @Operation(summary = "Update hotel details", description = "Modify hotel information")
    public ResponseEntity<HotelResponseDTO> getHotel(@PathVariable Long hotelId){
        HotelResponseDTO hotel=hotelService.getHotel(hotelId);
        return new ResponseEntity<>(hotel,HttpStatus.FOUND);
    }
    
    @PutMapping("/{hotelId}")
    @Operation(summary = "Update hotel details", description = "Modify hotel information")
    public ResponseEntity<HotelResponseDTO> updateHotel(@RequestBody HotelRequestDTO hotelRequestDTO, @PathVariable Long hotelId){
        HotelResponseDTO hotelResponseDTO=hotelService.updateHotelById(hotelRequestDTO,hotelId);
        return ResponseEntity.ok(hotelResponseDTO);
    }

    @DeleteMapping("/{hotelId}")
    @Operation(summary = "Delete a hotel", description = "Removes a hotel from the system")
    public ResponseEntity<String> deleteHotel(@PathVariable Long hotelId){
        return ResponseEntity.ok(hotelService.deleteHotelById(hotelId));
    }


    @GetMapping
    @Operation(summary = "Get hotel by ID", description = "Fetch details of a specific hotel")
    public ResponseEntity<List<HotelResponseDTO>> getAllHotels(){
        return ResponseEntity.ok(hotelService.getAllHotel());
    }

    @PatchMapping("/{hotelId}/activate")
    @Operation(summary = "Activate a hotel", description = "Marks a hotel as active")
    public ResponseEntity<String> activateHotel(@PathVariable Long hotelId){
        return ResponseEntity.ok(hotelService.activateHotelById(hotelId));
    }

}
