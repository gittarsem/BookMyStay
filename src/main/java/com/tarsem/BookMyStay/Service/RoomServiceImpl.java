package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.Exceptions.ResourceNotFoundException;
import com.tarsem.BookMyStay.Exceptions.UnAuthorisedException;
import com.tarsem.BookMyStay.Repositroy.HotelRepo;
import com.tarsem.BookMyStay.Repositroy.RoomRepo;
import com.tarsem.BookMyStay.Service.Interfaces.InventoryService;
import com.tarsem.BookMyStay.Service.Interfaces.RoomService;
import com.tarsem.BookMyStay.dto.RoomDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static com.tarsem.BookMyStay.Utils.AppUtils.verifyHotelOwner;

@Service
@Slf4j
@AllArgsConstructor
public class RoomServiceImpl implements RoomService {

    @Autowired
    private HotelRepo hotelRepo;

    @Autowired
    private RoomRepo roomRepo;

    @Autowired
    private InventoryService inventoryService;

    private final ModelMapper modelMapper;



    @Override
    public RoomDTO addNewRoom(RoomDTO roomDTO, Long hotelId) {
        log.info("Creating a new room in hotel with ID: {}", hotelId);
        HotelEntity hotel=hotelRepo.findById(hotelId).orElseThrow(
            ()->new ResourceNotFoundException("Hotel with this Id does not exist")
        );
        if(!verifyHotelOwner(hotel)) throw new UnAuthorisedException("This user does not own this hotel");
        RoomEntity room=modelMapper.map(roomDTO,RoomEntity.class);
        room.setHotel(hotel);
        roomRepo.save(room);
        if(hotel.getActive()){
            inventoryService.initializeRoom(room);
        }
        return modelMapper.map(room,RoomDTO.class);
    }

    @Override
    public List<RoomDTO> giveAllRoomsInHotel(Long hotelId) {
        log.info("Getting all rooms in hotel with ID: {}", hotelId);
        HotelEntity hotel=hotelRepo.findById(hotelId).orElseThrow(
                ()-> new ResourceNotFoundException("Hotel with this id does not exist")
        );
        if(!verifyHotelOwner(hotel)) throw new UnAuthorisedException("This user does not own this hotel");
        return hotel.getRooms()
                .stream()
                .map((el)-> modelMapper.map(el,RoomDTO.class))
                .toList();
    }

    @Override
    public RoomDTO getRoomById(Long hotelId, Long roomId) {
        log.info("Getting the room with ID: {}", roomId);
        HotelEntity hotel=hotelRepo.findById(hotelId).orElseThrow(
                ()-> new ResourceNotFoundException("Hotel with this id does not exist")
        );
        RoomEntity room=roomRepo.findById(roomId).orElseThrow(
                ()-> new ResourceNotFoundException("Room does not exist")
        );
        return modelMapper.map(room,RoomDTO.class);
    }

    @Override
    @Transactional
    public RoomDTO updateRoomById(Long hotelId, Long roomId, RoomDTO roomDTO) {
        log.info("Updating the room with ID: {}", roomId);
        HotelEntity hotel=hotelRepo.findById(hotelId).orElseThrow(
                ()-> new ResourceNotFoundException("Hotel with this id does not exist")
        );
        if(!verifyHotelOwner(hotel)) throw new UnAuthorisedException("This user does not own this hotel");
        RoomEntity room=roomRepo.findById(roomId).orElseThrow(
                ()-> new ResourceNotFoundException("Room with this id does not exist")
        );
        modelMapper.map(roomDTO,room);
        room.setId(roomId);
        roomRepo.save(room);
        return modelMapper.map(room,RoomDTO.class);
    }


    @Override
    @Transactional
    public String deleteRoomById(Long hotelId,Long roomId) {
        log.info("Deleting the room with ID: {}", roomId);
        HotelEntity hotel=hotelRepo.findById(hotelId).orElseThrow(
                ()-> new ResourceNotFoundException("Hotel with this id does not exist")
        );
        RoomEntity room=roomRepo.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("Room not found with ID: "+roomId)
        );
        if(!verifyHotelOwner(hotel)) throw new UnAuthorisedException("This user does not own this hotel");
        inventoryService.deleteAllInventories(room);
        roomRepo.deleteById(roomId);
        return ("Deleted Room with id: "+roomId);
    }

}
