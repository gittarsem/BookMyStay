package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.dto.HotelPriceDTO;
import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.Exceptions.ResourceNotFoundException;
import com.tarsem.BookMyStay.Repositroy.HotelMinPriceRepository;
import com.tarsem.BookMyStay.Repositroy.InventoryRepo;
import com.tarsem.BookMyStay.Repositroy.RoomRepo;
import com.tarsem.BookMyStay.Service.Interfaces.InventoryService;
import com.tarsem.BookMyStay.dto.HotelSearchRequest;
import com.tarsem.BookMyStay.dto.InventoryDTO;
import com.tarsem.BookMyStay.dto.InventoryUpdateRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.tarsem.BookMyStay.Utils.AppUtils.verifyHotelOwner;

@Service
@Slf4j
@AllArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepo inventoryRepo;
    private final RoomRepo roomRepo;
    private final ModelMapper modelMapper;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private static final int DAYS_AHEAD=30;

    @Override
    @Transactional
    public void initializeRoom(RoomEntity room){
        LocalDate today=LocalDate.now();
        LocalDate endDate=today.plusDays(DAYS_AHEAD);
        for(LocalDate date=today;!date.isAfter(endDate); date=date.plusDays(1)){
           inventoryRepo.initializeRoom(
                   room.getId(),
                   room.getHotel().getId(),
                   room.getHotel().getHotelContactInfo().getLocation(),
                   date,
                   0,
                   0,
                   room.getCapacity(),
                   BigDecimal.ONE,
                   room.getPrice(),
                   false
           );
        }

    }
    @Transactional
    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduledInventoryJob() {
        List<RoomEntity> rooms = roomRepo.findAll();
        for (RoomEntity room : rooms) {
            initializeRoom(room);
        }
    }

    @Override
    public void deleteAllInventories(RoomEntity room){
        log.info("Deleting the inventories of room with id: {}", room.getId());
        inventoryRepo.deleteByRoom(room);
    }

    @Override
    public Page<HotelPriceDTO> searchHotels(HotelSearchRequest hotelSearchRequest) {
        log.info("Searching hotels for {} city, from {} to {}", hotelSearchRequest.getCity(), hotelSearchRequest.getStartDate(), hotelSearchRequest.getEndDate());
        Pageable pageable = PageRequest.of(hotelSearchRequest.getPage(), hotelSearchRequest.getSize());
        long dateCount =
                ChronoUnit.DAYS.between(hotelSearchRequest.getStartDate(), hotelSearchRequest.getEndDate()) + 1;

        Page<HotelPriceDTO> hotelPage = hotelMinPriceRepository.findHotelsWithAvailableInventory(hotelSearchRequest.getCity(),
                hotelSearchRequest.getStartDate(), hotelSearchRequest.getEndDate(), hotelSearchRequest.getRoomsCount(),
                dateCount, pageable);

        return hotelPage.map((element) -> modelMapper.map(element, HotelPriceDTO.class));
    }

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
