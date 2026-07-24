package com.tarsem.BookMyStay.Service.Interfaces;

import com.tarsem.BookMyStay.Entity.BookingEntity;
import com.tarsem.BookMyStay.Entity.PaymentEntity;
import com.tarsem.BookMyStay.Exceptions.RoomNotAvailableException;
import com.tarsem.BookMyStay.dto.BookingCancelDTO;
import com.tarsem.BookMyStay.dto.BookingDTO;
import com.tarsem.BookMyStay.dto.BookingRequestDTO;
import com.tarsem.BookMyStay.dto.GuestDTO;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface BookingService {
    BookingDTO initializeBooking(BookingRequestDTO bookingRequestDTO) throws RoomNotAvailableException;

    @Nullable
    BookingDTO addGuests(Long bookingId, List<GuestDTO> guests);

    public void releaseInventory(Long bookingId);

    public void confirmInventory(Long bookingId);

    BookingCancelDTO cancelBooking(Long bookingId) throws IllegalStateException;

    public void expireBooking(BookingEntity booking);
}