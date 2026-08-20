package com.tarsem.BookMyStay.Service.Interfaces;

import com.tarsem.BookMyStay.dto.booking.BookingRequestDTO;
import com.tarsem.BookMyStay.dto.booking.PriceQuoteDTO;

public interface DailyBookingService {
    PriceQuoteDTO createPriceQuote(BookingRequestDTO request);
}
