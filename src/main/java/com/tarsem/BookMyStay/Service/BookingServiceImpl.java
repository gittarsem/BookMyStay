package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.InventoryEntity;
import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.Exceptions.ResourceNotFoundException;
import com.tarsem.BookMyStay.Exceptions.RoomNotAvailableException;
import com.tarsem.BookMyStay.Repositroy.BookingRepository;
import com.tarsem.BookMyStay.Repositroy.HotelRepository;
import com.tarsem.BookMyStay.Repositroy.InventoryRepository;
import com.tarsem.BookMyStay.Repositroy.RoomRepository;
import com.tarsem.BookMyStay.Service.Interfaces.BookingService;
import com.tarsem.BookMyStay.dto.BookingDTO;
import com.tarsem.BookMyStay.dto.BookingRequestDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private BookingRepository bookingRepository;
    private HotelRepository hotelRepository;
    private RoomRepository roomRepository;
    private InventoryRepository inventoryRepository;

    @Override
    public BookingDTO initializeBooking(BookingRequestDTO bookingRequest) throws RoomNotAvailableException {
        log.info("Initialising booking for hotel : {}, room: {}, date {}-{}", bookingRequest.getHotelId(),
                bookingRequest.getRoomId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());

        HotelEntity hotel=hotelRepository.findById(bookingRequest.getHotelId()).orElseThrow(
                ()-> new ResourceNotFoundException("Hotel does not exist with this ID: "+bookingRequest.getHotelId())
        );


        RoomEntity room=roomRepository.findById(bookingRequest.getRoomId()).orElseThrow(
                ()-> new ResourceNotFoundException("Room does not exist with this ID: "+bookingRequest.getRoomId())
        );

        if(!room.getHotel().equals(hotel)){
            throw new RuntimeException("Room does not exist in hotel");
        }

        List<InventoryEntity> inventoryEntityList=inventoryRepository.findAndLockAvailableInventory(
                bookingRequest.getRoomId(),bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate(),bookingRequest.getRoomsCount()
        );

        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate())+1;

        if(inventoryEntityList.size()!=daysCount){
            throw new RoomNotAvailableException("Room is not Available Anymore");
        }

        inventoryRepository.initBooking(
                bookingRequest.getRoomId(),bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate(),bookingRequest.getRoomsCount()
        );




        return null;
    }
}
