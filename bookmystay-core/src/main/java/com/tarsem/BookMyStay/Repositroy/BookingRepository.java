package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.BookingEntity;
import com.tarsem.BookMyStay.Entity.GuestEntity;
import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity,Long> {
    List<BookingEntity> findAllByUserOrderByCreatedAtDesc(UserEntity user);
    @Query(
            """
            SELECT b FROM BookingEntity b
            WHERE b.status=:status
            AND b.createdAt<=:cutOff
            """
    )
    public List<BookingEntity> findExpiredBooking(
            @Param("status") BookingStatus status,
            @Param("cutOff") LocalDateTime cutOff
            );

    List<BookingEntity> findAllByRoom_HotelOrderByCreatedAtDesc(HotelEntity hotel);

    List<BookingEntity> findAllByRoom_HotelAndStatusOrderByCreatedAtDesc(
            HotelEntity hotel,
            BookingStatus status
    );

    Optional<BookingEntity> findByIdAndRoom_Hotel(
            Long bookingId,
            HotelEntity hotel
    );

    void deleteByGuests(GuestEntity guest);

    @Query("""
    SELECT COALESCE(SUM(b.totalPrice), 0)
    FROM BookingEntity b
    WHERE b.hotel.id = :hotelId
      AND b.status = :status
""")
    BigDecimal getTotalRevenue(
            @Param("hotelId") Long hotelId,
            @Param("status") BookingStatus status
    );

    @Query("""
    SELECT COALESCE(SUM(b.totalPrice), 0)
    FROM BookingEntity b
    WHERE b.hotel.id = :hotelId
      AND b.status = :status
      AND b.createdAt >= :startDate
      AND b.createdAt < :endDate
""")
    BigDecimal getPeriodRevenue(
            @Param("hotelId") Long hotelId,
            @Param("status") BookingStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
    SELECT COUNT(b)
    FROM BookingEntity b
    WHERE b.hotel.id = :hotelId
      AND b.status = :status
""")
    Long getTotalConfirmedBookings(
            @Param("hotelId") Long hotelId,
            @Param("status") BookingStatus status
    );

    @Query("""
    SELECT COUNT(b)
    FROM BookingEntity b
    WHERE b.hotel.id = :hotelId
      AND b.status = :status
      AND b.createdAt >= :startDate
      AND b.createdAt < :endDate
""")
    Long getPeriodConfirmedBookings(
            @Param("hotelId") Long hotelId,
            @Param("status") BookingStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query(value = """
    SELECT
        DATE(b.created_at) AS revenue_date,
        COALESCE(SUM(b.total_price), 0) AS revenue,
        COUNT(b.id) AS bookings
    FROM bookings b
    WHERE b.hotel_id = :hotelId
      AND b.status = 'CONFIRMED'
      AND b.created_at >= :startDate
      AND b.created_at < :endDate
    GROUP BY DATE(b.created_at)
    ORDER BY DATE(b.created_at)
    """, nativeQuery = true)
    List<Object[]> getRevenueTrend(
            @Param("hotelId") Long hotelId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
