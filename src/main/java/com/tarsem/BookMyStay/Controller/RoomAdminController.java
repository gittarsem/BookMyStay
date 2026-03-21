package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Service.Interfaces.RoomService;
import com.tarsem.BookMyStay.dto.RoomDTO;
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
@RequestMapping("admin/{hotelId}/rooms")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Room Admin Management", description = "Admin Manage rooms/hotels/inventories in a hotel")
public class RoomAdminController {

    @Autowired
    private RoomService roomService;

    @PostMapping
    @Operation(summary = "Create a new room", description = "Adds a new room to a specific hotel")
    public ResponseEntity<RoomDTO> addRoom(@PathVariable Long hotelId,@RequestBody RoomDTO roomDTO){
        RoomDTO room=roomService.addNewRoom(roomDTO,hotelId);
        return new ResponseEntity<>(roomDTO, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Retrieve all rooms in a hotel", description = "Fetches all rooms belonging to the specified hotel")
    public ResponseEntity<List<RoomDTO>> getAllRoomsInHotel(@PathVariable Long hotelId){
        return ResponseEntity.ok(roomService.giveAllRoomsInHotel(hotelId));
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "Retrieve room by Id in a hotel", description = "Fetches rooms belonging to the specified hotel")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long hotelId,
                                               @PathVariable Long roomId){
        return ResponseEntity.ok(roomService.getRoomById(hotelId,roomId));
    }

    @PutMapping("/{roomId}")
    @Operation(summary = "Update a room",
            description = "Updates the details of an existing room")
    public ResponseEntity<RoomDTO> updateRoomById(@PathVariable Long hotelId,
                                                  @PathVariable Long roomId, @RequestBody RoomDTO roomDTO){
        return ResponseEntity.ok(roomService.updateRoomById(hotelId,roomId,roomDTO));
    }

    @DeleteMapping("/{roomId}")
    @Operation(summary = "Delete a room",
            description = "Deletes a room from the hotel by ID")
    public ResponseEntity<String> deleteRoomById(@PathVariable Long hotelId, @PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.deleteRoomById(hotelId,roomId));
    }


}
