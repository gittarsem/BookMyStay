package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Service.Interfaces.RoomTypePricingService;
import com.tarsem.BookMyStay.dto.hotel.RoomTypeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/owner/{hotelId}/room-types/pricing")
@SecurityRequirement(name = "BearerAuth")
@Tag(
        name = "Room Type Pricing Management",
        description = "Owner manages hourly and daily pricing for room types"
)
public class RoomTypePricingController {

    private final RoomTypePricingService roomTypePricingService;

    @GetMapping
    @Operation(
            summary = "Get all room type pricing"
    )
    public ResponseEntity<List<RoomTypeDTO>> getAllPricing(
            @PathVariable Long hotelId
    ) {

        return ResponseEntity.ok(
                roomTypePricingService.getAllPricing(hotelId)
        );
    }

    @GetMapping("/{pricingId}")
    @Operation(
            summary = "Get room type pricing"
    )
    public ResponseEntity<RoomTypeDTO> getPricing(
            @PathVariable Long hotelId,
            @PathVariable Long pricingId
    ) {

        return ResponseEntity.ok(
                roomTypePricingService.getPricing(
                        hotelId,
                        pricingId
                )
        );
    }

    @PostMapping
    @Operation(
            summary = "Create room type pricing"
    )
    public ResponseEntity<RoomTypeDTO> createPricing(
            @PathVariable Long hotelId,
            @RequestBody RoomTypeDTO roomTypeDTO
    ) {

        return new ResponseEntity<>(
                roomTypePricingService.createPricing(
                        hotelId,
                        roomTypeDTO
                ),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{pricingId}")
    @Operation(
            summary = "Update room type pricing"
    )
    public ResponseEntity<RoomTypeDTO> updatePricing(
            @PathVariable Long hotelId,
            @PathVariable Long pricingId,
            @RequestBody RoomTypeDTO roomTypeDTO
    ) {

        return ResponseEntity.ok(
                roomTypePricingService.updatePricing(
                        hotelId,
                        pricingId,
                        roomTypeDTO
                )
        );
    }

    @DeleteMapping("/{pricingId}")
    @Operation(
            summary = "Delete room type pricing"
    )
    public ResponseEntity<String> deletePricing(
            @PathVariable Long hotelId,
            @PathVariable Long pricingId
    ) {

        return ResponseEntity.ok(
                roomTypePricingService.deletePricing(
                        hotelId,
                        pricingId
                )
        );
    }
}