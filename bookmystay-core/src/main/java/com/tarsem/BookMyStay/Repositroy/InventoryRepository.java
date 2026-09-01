package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.InventoryEntity;
import com.tarsem.BookMyStay.Entity.RoomEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository
        extends JpaRepository<InventoryEntity, Long> {

    List<InventoryEntity> findByRoomOrderByDate(RoomEntity room);


    @Modifying
    @Query("""
            UPDATE InventoryEntity i
            SET i.surgeFactor = :surgeFactor,
                i.closed = :closed
            WHERE i.room.id = :roomId
              AND i.date BETWEEN :startDate AND :endDate
            """)
    void updateInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("surgeFactor") BigDecimal surgeFactor,
            @Param("closed") Boolean closed
    );


    void deleteByRoom(RoomEntity room);


    @Modifying
    @Transactional
    @Query(value = """
    INSERT INTO inventory
    (
        room_id,
        hotel_id,
        city,
        date,
        book_count,
        reserved_count,
        total_count,
        surge_factor,
        price,
        closed,
        created_at
    )
    SELECT
        :roomId,
        :hotelId,
        :city,
        gs::date,
        0,
        0,
        :totalCount,
        1.00,
        :price,
        false,
        CURRENT_TIMESTAMP
    FROM generate_series(
        :startDate,
        :endDate,
        INTERVAL '1 day'
    ) gs
    ON CONFLICT (room_id, date) DO NOTHING
    """, nativeQuery = true)
    void initializeRoomInventory(
            @Param("roomId") Long roomId,
            @Param("hotelId") Long hotelId,
            @Param("city") String city,
            @Param("totalCount") Integer totalCount,
            @Param("price") BigDecimal price,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    /*
     * Used while generating a price quote.
     * NO database lock.
     */
    @Query("""
            SELECT i
            FROM InventoryEntity i
            WHERE i.room.id = :roomId
              AND i.closed = false
              AND i.date >= :startDate
              AND i.date < :endDate
              AND (i.totalCount - i.bookCount - i.reservedCount) > 0
            ORDER BY i.date
            """)
    List<InventoryEntity> findAvailableInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    /*
     * Used during actual booking.
     * Locks inventory rows using FOR UPDATE.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT i
        FROM InventoryEntity i
        WHERE i.room.id = :roomId
          AND i.closed = false
          AND i.date >= :startDate
          AND i.date < :endDate
          AND (i.totalCount - i.bookCount - i.reservedCount) > 0
        ORDER BY i.date
        """)
    List<InventoryEntity> findAndLockAvailableInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    /*
     * Reserve inventory.
     */
    @Modifying
    @Query("""
        UPDATE InventoryEntity i
        SET i.reservedCount = i.reservedCount + 1
        WHERE i.room.id = :roomId
          AND i.date >= :startDate
          AND i.date < :endDate
          AND i.closed = false
          AND (i.totalCount - i.bookCount - i.reservedCount) > 0
        """)
    int initBooking(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    @Query("""
            SELECT MAX(i.date)
            FROM InventoryEntity i
            WHERE i.room.id = :roomId
            """)
    LocalDate findLastInventoryDate(
            @Param("roomId") Long roomId
    );


    /*
     * Release PAYMENT_PENDING reservation.
     */
    @Modifying
    @Query("""
            UPDATE InventoryEntity i
            SET i.reservedCount = i.reservedCount - 1
            WHERE i.room.id = :roomId
              AND i.date >= :startDate
              AND i.date < :endDate
              AND i.reservedCount > 0
            """)
    int releaseReservation(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    /*
     * Convert reservation into confirmed booking.
     */
    @Modifying
    @Query("""
            UPDATE InventoryEntity i
            SET i.reservedCount = i.reservedCount - 1,
                i.bookCount = i.bookCount + 1
            WHERE i.room.id = :roomId
              AND i.date >= :startDate
              AND i.date < :endDate
              AND i.reservedCount > 0
            """)
    int confirmReservation(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    /*
     * Cancel confirmed booking.
     */
    @Modifying
    @Query("""
            UPDATE InventoryEntity i
            SET i.bookCount = i.bookCount - 1
            WHERE i.room.id = :roomId
              AND i.date >= :startDate
              AND i.date < :endDate
              AND i.bookCount > 0
            """)
    int cancelBooking(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT i
        FROM InventoryEntity i
        WHERE i.room.id = :roomId
          AND i.date = :date
          AND i.closed = false
          AND (i.totalCount - i.bookCount - i.reservedCount) > 0
        """)
    Optional<InventoryEntity> findAvailableInventoryForDate(
            @Param("roomId") Long roomId,
            @Param("date") LocalDate date
    );

    List<InventoryEntity> findByHotelIdAndDateBetweenOrderByDate(
            Long hotelId,
            LocalDate startDate,
            LocalDate endDate
    );
}