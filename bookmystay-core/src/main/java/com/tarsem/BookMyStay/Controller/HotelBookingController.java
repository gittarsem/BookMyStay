package com.tarsem.BookMyStay.Controller;

import com.razorpay.RazorpayException;
import com.tarsem.BookMyStay.Entity.BookingEntity;
import com.tarsem.BookMyStay.Enums.BookingMode;
import com.tarsem.BookMyStay.Exceptions.BusinessRuleViolationException;
import com.tarsem.BookMyStay.Exceptions.RoomNotAvailableException;
import com.tarsem.BookMyStay.Repositroy.GuestRepository;
import com.tarsem.BookMyStay.Service.Interfaces.BookingService;
import com.tarsem.BookMyStay.Service.Interfaces.DailyBookingService;
import com.tarsem.BookMyStay.Service.Interfaces.HourlyBookingService;
import com.tarsem.BookMyStay.dto.booking.*;
import com.tarsem.BookMyStay.dto.hotel.GuestDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/guest/bookings")
@RequiredArgsConstructor
public class HotelBookingController {

    @Autowired
    private BookingService bookingService;
    private final DailyBookingService dailyBookingService;
    private final HourlyBookingService hourlyBookingService;

    @PostMapping("/init")
    public ResponseEntity<BookingDTO> initBooking(
            @RequestBody BookingRequestDTO request
    ) {
        return ResponseEntity.ok(
                bookingService.initializeBooking(request)
        );
    }

    @PostMapping("/daily-quote")
    public ResponseEntity<PriceQuoteDTO> dailyQuote(
            @RequestBody BookingRequestDTO request
    ) {
        return ResponseEntity.ok(
                dailyBookingService.createPriceQuote(request)
        );
    }

    @PostMapping("/hourly-quote")
    public ResponseEntity<PriceQuoteDTO> hourlyQuote(
            @RequestBody BookingRequestDTO request
    ) {
        return ResponseEntity.ok(
                hourlyBookingService.createPriceQuote(request)
        );
    }

    @GetMapping("/{bookingId}/cancellation-preview")
    public ResponseEntity<CancellationPreviewDTO> getCancellationPreview(
            @PathVariable Long bookingId
    ) throws AccessDeniedException {

        return ResponseEntity.ok(
                bookingService.getCancellationPreview(bookingId)
        );
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingCancelDTO> cancelBooking(@PathVariable Long bookingId) throws AccessDeniedException, RazorpayException {
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

    @PostMapping("/{bookingId}/addGuests")
    public ResponseEntity<BookingDTO> addGuest(
            @PathVariable Long bookingId,
            @RequestBody List<GuestDTO> guests
    ){
        return ResponseEntity.ok(bookingService.addGuests(bookingId,guests));
    }

    @PutMapping("/{bookingId}/guests/{guestId}")
    public ResponseEntity<GuestDTO> updateGuestDetails(
            @PathVariable Long bookingId,
            @PathVariable Long guestId,
            @RequestBody GuestDTO request){
        return ResponseEntity.ok(bookingService.updateGuestDetails(bookingId,guestId,request));
    }

    @DeleteMapping("/{bookingId}/delete/{guestId}")
    public ResponseEntity<String> deleteGuestDetails(
            @PathVariable Long bookingId,
            @PathVariable Long guestId
            ){
        return ResponseEntity.ok(bookingService.deleteGuestDetails(bookingId,guestId));
    }

}
