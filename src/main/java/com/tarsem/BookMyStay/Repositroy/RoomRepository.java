package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.Enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity,Long> {

    @Query("SELECT MIN(r.Price) FROM RoomEntity r WHERE r.hotel.id = :hotelId")
    BigDecimal findMinPriceByHotelId(Long hotelId);

    @Query( value = """
            SELECT r.*
            FROM room r
            JOIN inventory i ON i.room_id=r.id
            WHERE r.hotel_id=:hotelId
                AND r.room_type=:roomType
                AND r.capacity>=:capacity
                AND i.date BETWEEN :checkInDate AND :checkOutDate
                AND (i.total_count - i.book_count - i.reserved_count) > 0
                AND i.closed=false
            GROUP BY r.id
            HAVING COUNT(*)=:requiredDays
            ORDER BY r.capacity ASC
            LIMIT 1
            """, nativeQuery = true
    )
    Optional<RoomEntity> findSuitableRoom(
            @Param("hotelId") Long hotelId,
            @Param("roomType") RoomType roomType,
            @Param("capacity") int capacity,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("requiredDays") long requiredDays
    );
}
