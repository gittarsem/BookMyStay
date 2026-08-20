package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.InventoryEntity;
import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.Entity.RoomTypePricingEntity;
import com.tarsem.BookMyStay.Enums.BookingMode;
import com.tarsem.BookMyStay.Exceptions.BusinessRuleViolationException;
import com.tarsem.BookMyStay.Exceptions.HotelNotFoundException;
import com.tarsem.BookMyStay.Exceptions.RoomNotAvailableException;
import com.tarsem.BookMyStay.Repositroy.HotelRepository;
import com.tarsem.BookMyStay.Repositroy.InventoryRepository;
import com.tarsem.BookMyStay.Repositroy.RoomRepository;
import com.tarsem.BookMyStay.Repositroy.RoomTypePricingRepository;
import com.tarsem.BookMyStay.Service.Interfaces.DailyBookingService;
import com.tarsem.BookMyStay.Strategy.PricingService;
import com.tarsem.BookMyStay.dto.booking.BookingRequestDTO;
import com.tarsem.BookMyStay.dto.booking.PriceQuoteDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyBookingServiceImpl
        implements DailyBookingService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final RoomTypePricingRepository roomTypePricingRepository;
    private final PricingService pricingService;
    private final PriceQuoteService priceQuoteService;

    @Override
    @Transactional
    public PriceQuoteDTO createPriceQuote(
            BookingRequestDTO request
    ) {

        log.info(
                "Creating DAILY quote: hotel={}, room={}, dates={}-{}",
                request.getHotelId(),
                request.getRoomType(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        validateRequest(request);

        HotelEntity hotel =
                hotelRepository.findById(request.getHotelId())
                        .orElseThrow(() ->
                                new HotelNotFoundException(
                                        "Hotel does not exist with this ID: "
                                                + request.getHotelId()
                                )
                        );

        int capacity =
                request.getAdultCount()
                        + request.getChildCount();

        List<RoomEntity> rooms =
                roomRepository.findDailyCandidateRooms(
                        hotel.getId(),
                        request.getRoomType(),
                        capacity
                );

        long requiredDays =
                ChronoUnit.DAYS.between(
                        request.getCheckInDate(),
                        request.getCheckOutDate()
                );

        LocalDate inventoryEndDate =
                request.getCheckOutDate();

        RoomEntity selectedRoom = null;
        List<InventoryEntity> selectedInventory = null;

        for (RoomEntity room : rooms) {

            List<InventoryEntity> inventory =
                    inventoryRepository.findAndLockAvailableInventory(
                            room.getId(),
                            request.getCheckInDate(),
                            inventoryEndDate
                    );

            if (inventory.size() == requiredDays) {
                selectedRoom = room;
                selectedInventory = inventory;
                break;
            }
        }

        if (selectedRoom == null) {
            throw new RoomNotAvailableException(
                    "No room is available for the selected dates."
            );
        }

        RoomTypePricingEntity pricing =
                roomTypePricingRepository
                        .findByHotelIdAndRoomType(
                                hotel.getId(),
                                request.getRoomType()
                        )
                        .orElseThrow(() ->
                                new BusinessRuleViolationException(
                                        "Pricing not configured for this room type."
                                )
                        );

        BigDecimal dailyPrice =
                pricing.getDailyPrice();

        if (dailyPrice == null
                || dailyPrice.compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessRuleViolationException(
                    "Daily pricing is not configured correctly."
            );
        }

        BigDecimal finalPrice =
                pricingService.calculateDailyTotal(
                        dailyPrice,
                        selectedInventory
                );

        return createQuote(
                request,
                hotel,
                selectedRoom,
                finalPrice
        );
    }

    private void validateRequest(
            BookingRequestDTO request
    ) {

        if (request.getBookingMode()
                != BookingMode.DAILY) {

            throw new BusinessRuleViolationException(
                    "Booking mode must be DAILY."
            );
        }

        if (request.getCheckInDate() == null
                || request.getCheckOutDate() == null) {

            throw new BusinessRuleViolationException(
                    "Check-in and check-out dates are required."
            );
        }

        if (!request.getCheckInDate()
                .isBefore(request.getCheckOutDate())) {

            throw new BusinessRuleViolationException(
                    "Check-out date must be after check-in date."
            );
        }

        if (request.getCheckInTime() != null
                || request.getCheckOutTime() != null) {

            throw new BusinessRuleViolationException(
                    "Time must not be provided for daily bookings."
            );
        }

        if (request.getRoomType() == null) {

            throw new BusinessRuleViolationException(
                    "Room type is required."
            );
        }

        if (request.getAdultCount()
                + request.getChildCount() <= 0) {

            throw new BusinessRuleViolationException(
                    "At least one guest is required."
            );
        }
    }

    private PriceQuoteDTO createQuote(
            BookingRequestDTO request,
            HotelEntity hotel,
            RoomEntity room,
            BigDecimal price
    ) {

        PriceQuoteDTO quote =
                PriceQuoteDTO.builder()
                        .hotelId(hotel.getId())
                        .roomId(room.getId())
                        .finalPrice(price)
                        .checkInDate(request.getCheckInDate())
                        .checkOutDate(request.getCheckOutDate())
                        .checkInTime(null)
                        .checkOutTime(null)
                        .roomType(request.getRoomType().name())
                        .bookingMode(BookingMode.DAILY.name())
                        .build();

        return priceQuoteService.createQuote(quote);
    }
}
