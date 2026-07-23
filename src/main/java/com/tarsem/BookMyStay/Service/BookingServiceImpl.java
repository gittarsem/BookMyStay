package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.*;
import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Exceptions.*;
import com.tarsem.BookMyStay.Repositroy.*;
import com.tarsem.BookMyStay.Service.Interfaces.BookingService;
import com.tarsem.BookMyStay.Strategy.PricingService;
import com.tarsem.BookMyStay.dto.BookingCancelDTO;
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
                bookingRequest.getRoomType(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());

        HotelEntity hotel=hotelRepository.findById(bookingRequest.getHotelId()).orElseThrow(
                ()-> new ResourceNotFoundException("Hotel does not exist with this ID: "+bookingRequest.getHotelId())
        );

        int totalCapReq=bookingRequest.getAdultCount()+bookingRequest.getChildCount();
        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate())+1;
        RoomEntity candidateRoom =roomRepository.findSuitableRoom(
                hotel.getId(),
                bookingRequest.getRoomType(),
                totalCapReq,
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(),
                daysCount
        ).orElseThrow(
                ()->new RoomNotAvailableException("Room is not Available")
        );


        List<InventoryEntity> inventoryEntityList=inventoryRepository.findAndLockAvailableInventory(
                candidateRoom.getId(), bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate()
        );


        if(inventoryEntityList.size()!=daysCount){
            throw new RoomNotAvailableException("Room is not Available Anymore");
        }

        inventoryRepository.initBooking(
                candidateRoom.getId(),bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate()
        );

        BigDecimal price = pricingService.calculateTotalPrice(inventoryEntityList);
        System.out.println(price);
        BookingEntity bookingEntity=BookingEntity.builder()
                .status(BookingStatus.PAYMENT_PENDING)
                .hotel(hotel)
                .room(candidateRoom)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .adultCount(bookingRequest.getAdultCount())
                .childCount(bookingRequest.getChildCount())
                .user(giveMeCurrentUser())
                .totalPrice(price)
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

        if(booking.getStatus()!=BookingStatus.BOOKED){
            throw new PaymentException("Payment has not be completed yet");
        }

        if(!booking.getGuests().isEmpty()){
            throw new GuestAlreadyAddedException("Guest has already been added");
        }

        int guestsCount=booking.getAdultCount()+booking.getChildCount();
        if(guests.size()>guestsCount){
            throw new IllegalArgumentException(
                    "Expected " + guestsCount + " guests but received " + guests.size()
            );
        }
        for(GuestDTO guest:guests){
            GuestEntity guest1=modelMapper.map(guest,GuestEntity.class);
            guest1.setUser(user);
            guestRepository.save(guest1);
            booking.getGuests().add(guest1);
        }
        booking.setUpdatedAt(LocalDateTime.now());
        booking=bookingRepository.save(booking);
        return modelMapper.map(booking,BookingDTO.class);

    }

    @Override
    @Transactional
    public void releaseInventory(Long bookingId){
        log.info("Releasing inventory for booking : {}",bookingId);

        BookingEntity booking=bookingRepository.findById(bookingId).orElseThrow(
                ()-> new ResourceNotFoundException("Booking does not exist with this ID: "+bookingId)
        );

        inventoryRepository.releaseReservation(booking.getRoom().getId(),booking.getCheckInDate(),booking.getCheckOutDate());
    }

    @Override
    @Transactional
    public void confirmInventory(Long bookingId){
        log.info("Confirming inventory for booking : {}",bookingId);

        BookingEntity booking=bookingRepository.findById(bookingId).orElseThrow(
                ()-> new ResourceNotFoundException("Booking does not exist with this ID: "+bookingId)
        );

        inventoryRepository.confirmReservation(booking.getRoom().getId(),booking.getCheckInDate(),booking.getCheckOutDate());
    }

    @Transactional
    public BookingCancelDTO cancelBooking(Long bookingId) throws IllegalStateException {
        log.info("Cancel request for booking : {}",bookingId);
        BookingEntity booking=bookingRepository.findById(bookingId).orElseThrow(
                ()->new ResourceNotFoundException("Booking does not exist for id :" + bookingId)
        );
        if(!booking.getUser().equals(giveMeCurrentUser())){
            throw new UnAuthorisedException("You are not authorised to cancel this booking");
        }
        if(booking.getStatus()!=BookingStatus.BOOKED){
            throw new IllegalStateException("Booking for this id is not booked or payment is pending");
        }
        inventoryRepository.cancelBooking(booking.getRoom().getId(),booking.getCheckInDate(),booking.getCheckOutDate());

        booking.setStatus(BookingStatus.CANCELLED);

        return BookingCancelDTO.builder()
                .bookingId(booking.getId())
                .bookingStatus(booking.getStatus())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .message("Booking cancelled successfully.")
                .build();

    }

}
