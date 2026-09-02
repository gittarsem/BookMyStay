package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Repositroy.BookingRepository;
import com.tarsem.BookMyStay.Service.Interfaces.RevenueService;
import com.tarsem.BookMyStay.dto.owner.OwnerRevenueDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RevenueServiceImpl implements RevenueService {

    private final BookingRepository bookingRepository;

    @Override
    public OwnerRevenueDTO getHotelRevenue(
            Long hotelId,
            LocalDate startDate,
            LocalDate endDate
    ) {

        LocalDateTime start =
                startDate.atStartOfDay();

        LocalDateTime end =
                endDate.plusDays(1).atStartOfDay();

        BigDecimal totalRevenue =
                bookingRepository.getTotalRevenue(
                        hotelId,
                        BookingStatus.BOOKED
                );

        BigDecimal periodRevenue =
                bookingRepository.getPeriodRevenue(
                        hotelId,
                        BookingStatus.BOOKED,
                        start,
                        end
                );

        Long totalBookings =
                bookingRepository.getTotalConfirmedBookings(
                        hotelId,
                        BookingStatus.BOOKED
                );

        BigDecimal averageBookingValue =
                totalBookings != null && totalBookings > 0
                        ? totalRevenue.divide(
                        BigDecimal.valueOf(totalBookings),
                        2,
                        java.math.RoundingMode.HALF_UP
                )
                        : BigDecimal.ZERO;

        List<Object[]> rows =
                bookingRepository.getRevenueTrend(
                        hotelId,
                        start,
                        end
                );

        List<OwnerRevenueDTO.RevenuePointDTO> trend =
                rows.stream()
                        .map(row -> new OwnerRevenueDTO.RevenuePointDTO(
                                ((java.sql.Date) row[0]).toLocalDate(),
                                (BigDecimal) row[1],
                                ((Number) row[2]).longValue()
                        ))
                        .toList();

        return new OwnerRevenueDTO(
                totalRevenue,
                periodRevenue,
                totalBookings,
                averageBookingValue,
                trend
        );
    }
}