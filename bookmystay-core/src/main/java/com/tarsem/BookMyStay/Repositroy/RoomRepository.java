package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.BookingEntity;
import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.Enums.RoomType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, Long> {

    Optional<RoomEntity> findByIdAndHotelId(
            Long roomId,
            Long hotelId
    );

    @Query("""
            SELECT r
            FROM RoomEntity r
            WHERE r.hotel.id = :hotelId
              AND r.roomType = :roomType
              AND r.capacity >= :capacity
            ORDER BY r.capacity ASC
            """)
    List<RoomEntity> findDailyCandidateRooms(
            @Param("hotelId") Long hotelId,
            @Param("roomType") RoomType roomType,
            @Param("capacity") int capacity
    );

    @Query(value = """
        SELECT r.*
        FROM room r
        WHERE r.hotel_id = :hotelId
          AND r.room_type = :roomType
          AND r.capacity >= :capacity

          AND EXISTS (
              SELECT 1
              FROM inventory i
              WHERE i.room_id = r.id
                AND i.date = :checkInDate
                AND i.closed = false
                AND (i.total_count - i.book_count - i.reserved_count) > 0
          )

          AND NOT EXISTS (
              SELECT 1
              FROM bookings b
              WHERE b.room_id = r.id
                AND b.status IN (:activeStatuses)
                AND (
                    (
                        b.check_in_date + b.check_in_time
                    ) < :checkOut
                    AND
                    (
                        b.check_out_date + b.check_out_time
                    ) > :checkIn
                )
          )

        ORDER BY r.capacity ASC
        LIMIT 1
        """, nativeQuery = true)
    Optional<RoomEntity> findAvailableRoom(
            @Param("hotelId") Long hotelId,
            @Param("roomType") String roomType,
            @Param("capacity") int capacity,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkIn") LocalDateTime checkIn,
            @Param("checkOut") LocalDateTime checkOut,
            @Param("activeStatuses") Collection<String> activeStatuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM RoomEntity r
            WHERE r.id = :roomId
            """)
    Optional<RoomEntity> findByIdForUpdateAndLock(
            @Param("roomId") Long roomId
    );

    @Query("""
            SELECT CASE
                WHEN COUNT(b) = 0 THEN true
                ELSE false
            END
            FROM BookingEntity b
            WHERE b.room.id = :roomId
              AND b.status IN :activeStatuses

              AND (
                    b.checkInDate < :checkOutDate
                    OR (
                        b.checkInDate = :checkOutDate
                        AND b.checkInTime < :checkOutTime
                    )
              )

              AND (
                    b.checkOutDate > :checkInDate
                    OR (
                        b.checkOutDate = :checkInDate
                        AND b.checkOutTime > :checkInTime
                    )
              )
            """)
    boolean isRoomAvailable(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkInTime") LocalTime checkInTime,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("checkOutTime") LocalTime checkOutTime,
            @Param("activeStatuses") Collection<String> activeStatuses
    );

    @Query(value = """
    SELECT DISTINCT r.hotel_id
    FROM room r
    WHERE NOT EXISTS (
        SELECT 1
        FROM bookings b
        WHERE b.room_id = r.id
          AND b.status IN (:activeStatuses)
          AND (
              b.check_in_date + b.check_in_time
          ) < :checkOut
          AND (
              b.check_out_date + b.check_out_time
          ) > :checkIn
    )
    """, nativeQuery = true)
    List<Long> findAvailableHotelIds(
            @Param("checkIn") LocalDateTime checkIn,
            @Param("checkOut") LocalDateTime checkOut,
            @Param("activeStatuses") Collection<String> activeStatuses
    );
}