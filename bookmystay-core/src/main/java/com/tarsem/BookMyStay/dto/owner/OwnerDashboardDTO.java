package com.tarsem.BookMyStay.dto.owner;

import com.tarsem.BookMyStay.dto.hotel.HotelResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerDashboardDTO {

    private Integer totalHotels;

    private Integer activeHotels;

    private Integer totalRooms;

    private Integer activeBookings;

    private BigDecimal totalRevenue;

    private List<OwnerHotelDashboardDTO> hotels;

    private List<OwnerBookingDTO> recentBookings;
}