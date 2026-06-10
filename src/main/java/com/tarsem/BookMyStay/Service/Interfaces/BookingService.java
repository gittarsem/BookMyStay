package com.tarsem.BookMyStay.Service.Interfaces;

import com.tarsem.BookMyStay.Exceptions.RoomNotAvailableException;
import com.tarsem.BookMyStay.dto.BookingDTO;
import com.tarsem.BookMyStay.dto.BookingRequestDTO;

public interface BookingService {
    BookingDTO initializeBooking(BookingRequestDTO bookingRequestDTO) throws RoomNotAvailableException;
}
