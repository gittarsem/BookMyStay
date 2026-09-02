package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Service.Interfaces.RevenueService;
import com.tarsem.BookMyStay.dto.owner.OwnerRevenueDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/owner/hotels/{hotelId}/revenue")
public class OwnerRevenueController {

    private final RevenueService revenueService;

    @GetMapping
    public ResponseEntity<OwnerRevenueDTO> getHotelRevenue(
            @PathVariable Long hotelId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return ResponseEntity.ok(
                revenueService.getHotelRevenue(
                        hotelId,
                        startDate,
                        endDate
                )
        );
    }
}