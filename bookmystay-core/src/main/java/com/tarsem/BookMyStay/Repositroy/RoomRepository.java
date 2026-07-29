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

    @Query("SELECT MIN(r.price) FROM RoomEntity r WHERE r.hotel.id = :hotelId")
    BigDecimal findMinPriceByHotelId(Long hotelId);

    @Query("""
    SELECT r
    FROM RoomEntity r
    JOIN r.inventories i
    WHERE r.hotel.id = :hotelId
      AND r.roomType = :roomType
      AND r.capacity >= :capacity
      AND i.date BETWEEN :checkInDate AND :checkOutDate
      AND (i.totalCount - i.bookCount - i.reservedCount) > 0
      AND i.closed = false
    GROUP BY r
    HAVING COUNT(i) = :requiredDays
    ORDER BY r.capacity ASC
""")
    Optional<RoomEntity> findSuitableRoom(
            @Param("hotelId") Long hotelId,
            @Param("roomType") RoomType roomType,
            @Param("capacity") int capacity,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("requiredDays") long requiredDays
    );

    Optional<RoomEntity> findByIdAndHotelId(Long roomId, Long hotelId);
}
