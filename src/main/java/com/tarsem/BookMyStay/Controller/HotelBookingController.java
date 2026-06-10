package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Exceptions.RoomNotAvailableException;
import com.tarsem.BookMyStay.Service.Interfaces.BookingService;
import com.tarsem.BookMyStay.dto.BookingDTO;
import com.tarsem.BookMyStay.dto.BookingRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class HotelBookingController {

    private BookingService bookingService;

    @PostMapping("/init")
    @Operation(summary = "Initialize a new booking", tags = {"Booking Flow"})
    public ResponseEntity<BookingDTO> initializeBooking(@RequestBody BookingRequestDTO bookingRequestDTO) throws RoomNotAvailableException{
        return ResponseEntity.ok(bookingService.initializeBooking(bookingRequestDTO));
    }
}
