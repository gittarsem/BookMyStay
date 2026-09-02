package com.tarsem.BookMyStay.dto.owner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerHotelDashboardDTO {

    private Long hotelId;

    private String hotelName;

    private String city;

    private boolean active;

    private Integer totalRooms;

    private Integer activeBookings;

    private BigDecimal revenue;
}