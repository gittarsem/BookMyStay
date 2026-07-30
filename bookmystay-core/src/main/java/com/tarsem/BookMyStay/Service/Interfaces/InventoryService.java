package com.tarsem.BookMyStay.Service.Interfaces;

import com.tarsem.BookMyStay.dto.hotel.HotelSearchResponseDTO;
import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.dto.inventory.InventoryDTO;
import com.tarsem.BookMyStay.dto.inventory.InventoryUpdateRequest;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public interface InventoryService {
    @Nullable List<InventoryDTO> getAllInventoryByRoom(Long roomId);

    @Nullable String updateInventory(Long roomId, InventoryUpdateRequest inventoryUpdateRequest);

    void initializeRoom(RoomEntity room);

    void deleteAllInventories(RoomEntity room);

    HotelSearchResponseDTO searchHotels(String keyword, String city, Double maxPrice, Double minPrice, Double ratings, String sortField, String sortOrder, int page, int size) throws IOException;

    public void scheduledInventoryJob();
}
