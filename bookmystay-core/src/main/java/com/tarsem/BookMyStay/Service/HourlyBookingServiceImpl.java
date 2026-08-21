package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.InventoryEntity;
import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.Entity.RoomTypePricingEntity;
import com.tarsem.BookMyStay.Enums.BookingMode;
import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Exceptions.BusinessRuleViolationException;
import com.tarsem.BookMyStay.Exceptions.HotelNotFoundException;
import com.tarsem.BookMyStay.Exceptions.RoomNotAvailableException;
import com.tarsem.BookMyStay.Repositroy.HotelRepository;
import com.tarsem.BookMyStay.Repositroy.InventoryRepository;
import com.tarsem.BookMyStay.Repositroy.RoomRepository;
import com.tarsem.BookMyStay.Repositroy.RoomTypePricingRepository;
import com.tarsem.BookMyStay.Service.Interfaces.HourlyBookingService;
import com.tarsem.BookMyStay.Strategy.PricingService;
import com.tarsem.BookMyStay.dto.booking.BookingRequestDTO;
import com.tarsem.BookMyStay.dto.booking.PriceQuoteDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HourlyBookingServiceImpl
        implements HourlyBookingService {

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
                "Creating HOURLY quote: hotel={}, room={}, date={} {}-{}",
                request.getHotelId(),
                request.getRoomType(),
                request.getCheckInDate(),
                request.getCheckInTime(),
                request.getCheckOutTime()
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

        LocalDateTime checkIn =
                LocalDateTime.of(
                        request.getCheckInDate(),
                        request.getCheckInTime()
                );

        LocalDateTime checkOut =
                LocalDateTime.of(
                        request.getCheckOutDate(),
                        request.getCheckOutTime()
                );

        validateBookingTime(
                checkIn,
                checkOut
        );

        int capacity =
                request.getAdultCount()
                        + request.getChildCount();

        Collection<String> activeStatuses =
                List.of(
                        BookingStatus.PAYMENT_PENDING.name(),
                        BookingStatus.BOOKED.name()
                );

        RoomEntity room =
                roomRepository.findAvailableRoom(
                        hotel.getId(),
                        request.getRoomType().name(),
                        capacity,
                        request.getCheckInDate(),
                        checkIn,
                        checkOut,
                        activeStatuses
                ).orElseThrow(() ->
                        new RoomNotAvailableException(
                                "No room is available for the selected time."
                        )
                );

        List<InventoryEntity> inventory =
                inventoryRepository.findAvailableInventory(
                        room.getId(),
                        request.getCheckInDate(),
                        request.getCheckInDate().plusDays(1)
                );

        if (inventory.size() != 1) {
            throw new RoomNotAvailableException(
                    "Room inventory is not available for the selected date."
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

        BigDecimal hourlyPrice =
                pricing.getHourlyPrice();

        if (hourlyPrice == null
                || hourlyPrice.compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessRuleViolationException(
                    "Hourly pricing is not configured correctly."
            );
        }

        long hours =
                ChronoUnit.HOURS.between(
                        checkIn,
                        checkOut
                );

        BigDecimal baseTotal =
                hourlyPrice.multiply(
                        BigDecimal.valueOf(hours)
                );

        BigDecimal finalPrice =
                pricingService.calculatePrice(
                        baseTotal,
                        inventory.get(0)
                );

        return createQuote(
                request,
                hotel,
                room,
                finalPrice
        );
    }

    private void validateRequest(
            BookingRequestDTO request
    ) {

        if (request.getBookingMode()
                != BookingMode.HOURLY) {

            throw new BusinessRuleViolationException(
                    "Booking mode must be HOURLY."
            );
        }

        if (request.getCheckInDate() == null
                || request.getCheckOutDate() == null) {

            throw new BusinessRuleViolationException(
                    "Check-in and check-out dates are required."
            );
        }

        if (request.getCheckInTime() == null
                || request.getCheckOutTime() == null) {

            throw new BusinessRuleViolationException(
                    "Check-in and check-out times are required for hourly bookings."
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

    private void validateBookingTime(
            LocalDateTime checkIn,
            LocalDateTime checkOut
    ) {

        if (!checkIn.toLocalDate()
                .equals(checkOut.toLocalDate())) {

            throw new BusinessRuleViolationException(
                    "Hourly booking must be within the same date."
            );
        }

        if (!checkIn.isBefore(checkOut)) {

            throw new BusinessRuleViolationException(
                    "Check-out time must be after check-in time."
            );
        }

        long minutes =
                ChronoUnit.MINUTES.between(
                        checkIn,
                        checkOut
                );

        if (minutes < 60) {
            throw new BusinessRuleViolationException(
                    "Hourly booking must be at least one hour."
            );
        }

        if (minutes % 60 != 0) {
            throw new BusinessRuleViolationException(
                    "Hourly booking duration must be in whole hours."
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
                        .checkInTime(request.getCheckInTime())
                        .checkOutTime(request.getCheckOutTime())
                        .roomType(request.getRoomType().name())
                        .bookingMode(BookingMode.HOURLY.name())
                        .build();

        return priceQuoteService.createQuote(quote);
    }
}
