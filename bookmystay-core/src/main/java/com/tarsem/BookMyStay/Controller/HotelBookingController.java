package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Exceptions.RoomNotAvailableException;
import com.tarsem.BookMyStay.Service.Interfaces.BookingService;
import com.tarsem.BookMyStay.dto.booking.*;
import com.tarsem.BookMyStay.dto.hotel.GuestDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/guest/bookings")
@RequiredArgsConstructor
public class HotelBookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/init")
    @Operation(summary = "Initialize a new booking", tags = {"Booking Flow"})
    public ResponseEntity<BookingDTO> initializeBooking(@RequestBody BookingRequestDTO bookingRequestDTO) throws RoomNotAvailableException{
        return ResponseEntity.ok(bookingService.initializeBooking(bookingRequestDTO));
    }

    @PostMapping("/{bookingId}/addGuests")
    public ResponseEntity<BookingDTO> addGuest(@PathVariable Long bookingId, @RequestBody List<GuestDTO> guests){
        return ResponseEntity.ok(bookingService.addGuests(bookingId,guests));
    }

    @PostMapping("/cancel/{bookingId}")
    public ResponseEntity<BookingCancelDTO> cancelBooking(@PathVariable Long bookingId){
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId));
    }

    @GetMapping
    public ResponseEntity<List<BookingHistoryDTO>> getMyBookings(){
        return ResponseEntity.ok(bookingService.getMyBookings());
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDetailsDTO> getBookingDetails(@PathVariable Long bookingId){
        return ResponseEntity.ok(bookingService.getBookingDetails(bookingId));
    }
}
