package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Service.Interfaces.InventoryService;
import com.tarsem.BookMyStay.dto.inventory.HotelInventoryDTO;
import com.tarsem.BookMyStay.dto.inventory.InventoryDTO;
import com.tarsem.BookMyStay.dto.inventory.InventoryUpdateRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/owner/inventory")
@Tag(
        name = "Owner Inventory",
        description = "Manage hotel room inventory"
)
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;


    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<InventoryDTO>> getAllInventoryByRoom(
            @PathVariable Long roomId
    ) {

        return ResponseEntity.ok(
                inventoryService.getAllInventoryByRoom(roomId)
        );
    }


    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<HotelInventoryDTO>> getHotelInventory(
            @PathVariable Long hotelId,

            @RequestParam LocalDate startDate,

            @RequestParam LocalDate endDate
    ) {

        return ResponseEntity.ok(
                inventoryService.getHotelInventory(
                        hotelId,
                        startDate,
                        endDate
                )
        );
    }


    @PatchMapping("/room/{roomId}")
    public ResponseEntity<String> updateInventory(
            @PathVariable Long roomId,
            @RequestBody InventoryUpdateRequest inventoryUpdateRequest
    ) {

        return ResponseEntity.ok(
                inventoryService.updateInventory(
                        roomId,
                        inventoryUpdateRequest
                )
        );
    }
}