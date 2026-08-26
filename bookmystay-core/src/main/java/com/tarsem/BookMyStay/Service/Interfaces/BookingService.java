package com.tarsem.BookMyStay.Service.Interfaces;

import com.razorpay.RazorpayException;
import com.tarsem.BookMyStay.Entity.BookingEntity;
import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Exceptions.RoomNotAvailableException;
import com.tarsem.BookMyStay.dto.booking.*;
import com.tarsem.BookMyStay.dto.hotel.GuestDTO;
import com.tarsem.BookMyStay.dto.owner.OwnerBookingDTO;
import com.tarsem.BookMyStay.dto.owner.OwnerBookingDetailsDTO;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface BookingService {
    BookingDTO initializeBooking(BookingRequestDTO bookingRequestDTO);

//    PriceQuoteDTO createPriceQuote(
//            BookingRequestDTO bookingRequest
//    );

    @Nullable
    BookingDTO addGuests(Long bookingId, List<GuestDTO> guests);

    public void releaseInventory(Long bookingId);

    public void confirmInventory(Long bookingId);

    BookingCancelDTO cancelBooking(Long bookingId) throws IllegalStateException, AccessDeniedException, RazorpayException;

    public void expireBooking(BookingEntity booking);

    List<BookingHistoryDTO> getMyBookings();

    BookingDetailsDTO getBookingDetails(Long bookingId);


    List<OwnerBookingDTO> getHotelBookings(
            Long hotelId,
            BookingStatus bookingStatus);

    OwnerBookingDetailsDTO getHotelBooking(Long hotelId, Long bookingId);

    GuestDTO updateGuestDetails(Long bookingId, Long guestId,GuestDTO request);

    String deleteGuestDetails(Long bookingId, Long guestId);

    CancellationPreviewDTO getCancellationPreview(Long bookingId);


//    PriceQuoteDTO createDailyPriceQuote(@Valid BookingRequestDTO request);
//
//    PriceQuoteDTO createHourlyPriceQuote(@Valid BookingRequestDTO request);
//
//    PriceQuoteDTO createPriceQuote(@Valid BookingRequestDTO bookingRequest);
}