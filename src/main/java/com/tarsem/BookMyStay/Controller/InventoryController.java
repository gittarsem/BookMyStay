package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Service.Interfaces.InventoryService;
import com.tarsem.BookMyStay.dto.InventoryDTO;
import com.tarsem.BookMyStay.dto.InventoryUpdateRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/inventory")
@Tag(name = "Admin Inventory", description = "Manage hotel room inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/room/{roomId")
    public ResponseEntity<List<InventoryDTO>> getAllInventoryByRoom(@PathVariable Long roomId){
        return ResponseEntity.ok(inventoryService.getAllInventoryByRoom(roomId));
    }

    @PatchMapping("/room/roomId")
    public ResponseEntity<String> updateInventory(@PathVariable Long roomId,
                                                  @RequestBody InventoryUpdateRequest inventoryUpdateRequest){
        return ResponseEntity.ok(inventoryService.updateInventory(roomId,inventoryUpdateRequest));

    }
}
