package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Service.Interfaces.BookingService;
import com.tarsem.BookMyStay.Service.Interfaces.HotelService;
import com.tarsem.BookMyStay.Service.Interfaces.OwnerDashboardService;
import com.tarsem.BookMyStay.Service.Interfaces.RoomService;
import com.tarsem.BookMyStay.dto.hotel.HotelResponseDTO;
import com.tarsem.BookMyStay.dto.hotel.RoomDTO;
import com.tarsem.BookMyStay.dto.owner.OwnerBookingDTO;
import com.tarsem.BookMyStay.dto.owner.OwnerDashboardDTO;
import com.tarsem.BookMyStay.dto.owner.OwnerHotelDashboardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerDashboardServiceImpl
        implements OwnerDashboardService {

    private final HotelService hotelService;

    private final RoomService roomService;

    private final BookingService bookingService;

    @Override
    public OwnerDashboardDTO getDashboard() {

        /*
         * HotelService already restricts getMyHotels()
         * to the currently authenticated owner.
         */
        List<HotelResponseDTO> hotels =
                hotelService.getMyHotels();

        int totalHotels = hotels.size();

        int activeHotels = 0;

        int totalRooms = 0;

        int activeBookings = 0;

        BigDecimal totalRevenue =
                BigDecimal.ZERO;

        List<OwnerHotelDashboardDTO> hotelSummaries =
                new ArrayList<>();

        List<OwnerBookingDTO> allBookings =
                new ArrayList<>();

        for (HotelResponseDTO hotel : hotels) {

            Long hotelId = hotel.getId();

            /*
             * -------------------------------------------------
             * ROOMS
             * -------------------------------------------------
             */

            List<RoomDTO> rooms =
                    roomService.giveAllRoomsInHotel(hotelId);

            int hotelRoomCount =
                    rooms != null ? rooms.size() : 0;

            totalRooms += hotelRoomCount;


            /*
             * -------------------------------------------------
             * HOTEL STATUS
             * -------------------------------------------------
             */

            if (hotel.isActive()) {
                activeHotels++;
            }


            /*
             * -------------------------------------------------
             * BOOKINGS
             * -------------------------------------------------
             */

            List<OwnerBookingDTO> hotelBookings =
                    bookingService.getHotelBookings(
                            hotelId,
                            null
                    );

            if (hotelBookings == null) {
                hotelBookings = new ArrayList<>();
            }

            allBookings.addAll(hotelBookings);


            /*
             * -------------------------------------------------
             * HOTEL BOOKING / REVENUE SUMMARY
             * -------------------------------------------------
             */

            int hotelActiveBookings = 0;

            BigDecimal hotelRevenue =
                    BigDecimal.ZERO;

            for (OwnerBookingDTO booking : hotelBookings) {

                /*
                 * CONFIRMED bookings are considered active
                 * bookings for the owner dashboard.
                 */
                if (booking.getBookingStatus()
                        == BookingStatus.BOOKED) {

                    hotelActiveBookings++;
                    activeBookings++;
                }

                /*
                 * Only PAID bookings contribute to revenue.
                 */
                if (booking.getPaymentStatus() != null
                        && booking.getPaymentStatus()
                        .name()
                        .equals("PAID")
                        && booking.getAmount() != null) {

                    hotelRevenue =
                            hotelRevenue.add(
                                    booking.getAmount()
                            );

                    totalRevenue =
                            totalRevenue.add(
                                    booking.getAmount()
                            );
                }
            }


            /*
             * -------------------------------------------------
             * HOTEL DASHBOARD SUMMARY
             * -------------------------------------------------
             */

            hotelSummaries.add(
                    OwnerHotelDashboardDTO.builder()
                            .hotelId(hotelId)
                            .hotelName(hotel.getName())
                            .city(hotel.getCity())
                            .active(hotel.isActive())
                            .totalRooms(hotelRoomCount)
                            .activeBookings(
                                    hotelActiveBookings
                            )
                            .revenue(hotelRevenue)
                            .build()
            );
        }


        /*
         * -----------------------------------------------------
         * RECENT BOOKINGS
         * -----------------------------------------------------
         *
         * OwnerBookingDTO doesn't contain createdAt,
         * therefore check-in date is used for ordering.
         */
        allBookings.sort(
                Comparator.comparing(
                        OwnerBookingDTO::getCheckInDate,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        List<OwnerBookingDTO> recentBookings =
                allBookings.stream()
                        .limit(5)
                        .toList();


        /*
         * -----------------------------------------------------
         * RESPONSE
         * -----------------------------------------------------
         */

        return OwnerDashboardDTO.builder()
                .totalHotels(totalHotels)
                .activeHotels(activeHotels)
                .totalRooms(totalRooms)
                .activeBookings(activeBookings)
                .totalRevenue(totalRevenue)
                .hotels(hotelSummaries)
                .recentBookings(recentBookings)
                .build();
    }
}