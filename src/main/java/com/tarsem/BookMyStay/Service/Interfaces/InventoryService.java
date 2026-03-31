package com.tarsem.BookMyStay.Service.Interfaces;

import com.tarsem.BookMyStay.dto.HotelPriceDTO;
import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.dto.HotelSearchRequest;
import com.tarsem.BookMyStay.dto.InventoryDTO;
import com.tarsem.BookMyStay.dto.InventoryUpdateRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {
    @Nullable List<InventoryDTO> getAllInventoryByRoom(Long roomId);

    @Nullable String updateInventory(Long roomId, InventoryUpdateRequest inventoryUpdateRequest);

    void initializeRoom(RoomEntity room);

    void deleteAllInventories(RoomEntity room);

    Page<HotelPriceDTO> searchHotels(HotelSearchRequest hotelSearchRequest);
}
