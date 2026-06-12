package com.tarsem.BookMyStay.Service.Interfaces;

import com.tarsem.BookMyStay.Exceptions.RoomNotAvailableException;
import com.tarsem.BookMyStay.dto.BookingDTO;
import com.tarsem.BookMyStay.dto.BookingRequestDTO;
import com.tarsem.BookMyStay.dto.GuestDTO;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface BookingService {
    BookingDTO initializeBooking(BookingRequestDTO bookingRequestDTO) throws RoomNotAvailableException;

    @Nullable BookingDTO addGuests(Long bookingId, List<GuestDTO> guests);
}
