package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.*;
import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Exceptions.ResourceNotFoundException;
import com.tarsem.BookMyStay.Exceptions.RoomNotAvailableException;
import com.tarsem.BookMyStay.Exceptions.UnAuthorisedException;
import com.tarsem.BookMyStay.Repositroy.*;
import com.tarsem.BookMyStay.Service.Interfaces.BookingService;
import com.tarsem.BookMyStay.Strategy.PricingService;
import com.tarsem.BookMyStay.dto.BookingDTO;
import com.tarsem.BookMyStay.dto.BookingRequestDTO;
import com.tarsem.BookMyStay.dto.GuestDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.tarsem.BookMyStay.Utils.AppUtils.giveMeCurrentUser;

@Service
@AllArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private BookingRepository bookingRepository;
    private HotelRepository hotelRepository;
    private RoomRepository roomRepository;
    private InventoryRepository inventoryRepository;
    private PricingService pricingService;
    private ModelMapper modelMapper;
    private GuestRepository guestRepository;

    @Override
    @Transactional
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
        BigDecimal price = pricingService.calculateTotalPrice(inventoryEntityList);
        System.out.println(price);
        BookingEntity bookingEntity=BookingEntity.builder()
                .status(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(giveMeCurrentUser())
                .totalPrice(price)
                .roomsCount(bookingRequest.getRoomsCount())
                .build();

        bookingRepository.save(bookingEntity);

        return modelMapper.map(bookingEntity,BookingDTO.class);
    }

    @Override
    @Transactional
    public @Nullable BookingDTO addGuests(Long bookingId, List<GuestDTO> guests) {
        log.info("Adding guests for booking with id: {}", bookingId);
        BookingEntity booking=bookingRepository.findById(bookingId).orElseThrow(
                ()-> new ResourceNotFoundException("Booking not found with this id: "+bookingId)
        );
        UserEntity user=giveMeCurrentUser();

        if(!user.equals(booking.getUser())){
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }
        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking is expired, cannot add guests");
        }
        if(booking.getStatus()!=BookingStatus.RESERVED){
            throw new IllegalStateException("Booking is not under reserved state, cannot add guests");
        }

        for(GuestDTO guest:guests){
            GuestEntity guest1=modelMapper.map(guest,GuestEntity.class);
            guest1.setUser(giveMeCurrentUser());
            guestRepository.save(guest1);
            booking.getGuests().add(guest1);
        }

        booking.setStatus(BookingStatus.GUESTS_ADDED);
        booking.setUpdatedAt(LocalDateTime.now());
        booking=bookingRepository.save(booking);
        return modelMapper.map(booking,BookingDTO.class);

    }

    private boolean hasBookingExpired(BookingEntity booking) {
        return booking.getCreatedAt().plusMinutes(10).equals(LocalDateTime.now());
    }
}
