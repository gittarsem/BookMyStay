package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Service.Interfaces.BookingService;
import com.tarsem.BookMyStay.dto.OwnerBookingDTO;
import com.tarsem.BookMyStay.dto.OwnerBookingDetailsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/owner/hotels/{hotelId}/bookings")
public class OwnerBookingController{
    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<OwnerBookingDTO>> getHotelBookings(
            @PathVariable Long hotelId,
            @RequestParam(required = false)
            BookingStatus bookingStatus) {

        return ResponseEntity.ok(
                bookingService.getHotelBookings(
                        hotelId,
                        bookingStatus));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<OwnerBookingDetailsDTO> getHotelBooking(
            @PathVariable Long hotelId,
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                bookingService.getHotelBooking(
                        hotelId,
                        bookingId));
    }
}
