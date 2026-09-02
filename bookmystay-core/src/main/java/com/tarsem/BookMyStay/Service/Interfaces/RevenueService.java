package com.tarsem.BookMyStay.Service.Interfaces;

import com.tarsem.BookMyStay.dto.owner.OwnerRevenueDTO;

import java.time.LocalDate;

public interface RevenueService {

    OwnerRevenueDTO getHotelRevenue(
            Long hotelId,
            LocalDate startDate,
            LocalDate endDate
    );
}