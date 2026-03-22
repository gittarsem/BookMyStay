package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.Exceptions.ResourceNotFoundException;
import com.tarsem.BookMyStay.Repositroy.InventoryRepo;
import com.tarsem.BookMyStay.Repositroy.RoomRepo;
import com.tarsem.BookMyStay.Service.Interfaces.InventoryService;
import com.tarsem.BookMyStay.dto.InventoryDTO;
import com.tarsem.BookMyStay.dto.InventoryUpdateRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.tarsem.BookMyStay.Utils.AppUtils.verifyHotelOwner;

@Service
@Slf4j
@AllArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepo inventoryRepo;
    private final RoomRepo roomRepo;
    private final ModelMapper modelMapper;


    @Override
    public List<InventoryDTO> getAllInventoryByRoom(Long roomId) {
        log.info("Getting All inventory by room for room with id: {}", roomId);
        RoomEntity room=roomRepo.findById(roomId).orElseThrow(
                ()-> new ResourceNotFoundException("Room with id "+roomId+" does not exist")
        );

        if(verifyHotelOwner(room.getHotel())) throw new AccessDeniedException("You are not the owner of room with id: "+roomId);
        return inventoryRepo.findByRoomOrderByDate(room)
                .stream()
                .map(
                        (element)->modelMapper.map(element,InventoryDTO.class)
                )
                .toList();

    }

    @Override
    public String updateInventory(Long roomId, InventoryUpdateRequest inventoryUpdateRequest) {
        log.info("Updating All inventory by room for room with id: {} between date range: {} - {}", roomId,
                inventoryUpdateRequest.getStartDate(),inventoryUpdateRequest.getEndDate());
        RoomEntity room=roomRepo.findById(roomId).orElseThrow(
                ()-> new ResourceNotFoundException("Room with id "+roomId+" does not exist")
        );

        if(verifyHotelOwner(room.getHotel())) throw new AccessDeniedException("You are not the owner of room with id: "+roomId);

        inventoryRepo.updateInventory(roomId,inventoryUpdateRequest.getStartDate(),
                inventoryUpdateRequest.getEndDate(),inventoryUpdateRequest.getSurgeFactor(),
                inventoryUpdateRequest.getClosed());
        return "Updated Room with id: " + roomId;
    }
}
