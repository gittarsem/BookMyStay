package com.tarsem.BookMyStay.dto.owner;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerRevenueDTO {

    private BigDecimal totalRevenue;

    private BigDecimal periodRevenue;

    private Long totalBookings;

    private BigDecimal averageBookingValue;

    private List<RevenuePointDTO> revenueTrend;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RevenuePointDTO {

        private LocalDate date;

        private BigDecimal revenue;

        private Long bookings;
    }
}