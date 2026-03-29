package com.tarsem.BookMyStay.Service.Interfaces;

import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.dto.InventoryDTO;
import com.tarsem.BookMyStay.dto.InventoryUpdateRequest;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface InventoryService {
    @Nullable List<InventoryDTO> getAllInventoryByRoom(Long roomId);

    @Nullable String updateInventory(Long roomId, InventoryUpdateRequest inventoryUpdateRequest);

    void initializeRoomforAYear(RoomEntity room);

    void deleteAllInventories(RoomEntity room);
}
