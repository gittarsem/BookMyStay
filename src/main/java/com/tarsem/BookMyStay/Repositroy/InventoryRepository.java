package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.InventoryEntity;
import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.dto.InventoryDTO;
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

public interface InventoryRepository extends JpaRepository<InventoryEntity,Long> {
    List<InventoryDTO> findByRoomOrderByDate(RoomEntity room);

    @Modifying
    @Query(
            """
                UPDATE InventoryEntity i
                SET i.surgeFactor = :surgeFactor,
                    i.closed = :closed
                WHERE i.room.id = :roomId
                  AND i.date BETWEEN :startDate AND :endDate
                   \s"""
    )
    void updateInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate")LocalDate endDate,
            @Param("surgeFactor")BigDecimal surgeFactor,
            @Param("closed")Boolean closed);


    void deleteByRoom(RoomEntity room);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO inventory
            (room_id,hotel_id,city,date,book_count,reserved_count,total_count,surge_factor,price,closed)
            VALUES(:roomId,:hotelId,:city,:date,:bookCount,:reservedCount,:totalCount,:surgeFactor,:price,:closed)
            ON CONFLICT(room_id,date) DO NOTHING
            """,nativeQuery = true
    )
    void initializeRoom(
            @Param("roomId") Long roomId,
            @Param("hotelId") Long hotelId,
            @Param("city") String city,
            @Param("date") LocalDate date,
            @Param("bookCount") Integer bookCount,
            @Param("reservedCount") Integer reservedCount,
            @Param("totalCount") Integer totalCount,
            @Param("surgeFactor") BigDecimal surgeFactor,
            @Param("price") BigDecimal price,
            @Param("closed") Boolean closed
    );

    @Query("""
            SELECT i FROM InventoryEntity i
            WHERE
             i.room.id= :roomId
             AND i.closed=false
             AND i.date BETWEEN :checkInDate AND :checkOutDate
             AND (i.totalCount-i.bookCount-i.reservedCount)>=:roomsCount
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<InventoryEntity> findAndLockAvailableInventory(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("roomsCount") Integer roomsCount
    );

    @Modifying
    @Query("""
            UPDATE InventoryEntity i
            SET i.reservedCount=i.reservedCount+ :numberOfRooms
            Where i.room.id=:roomId
            AND i.date BETWEEN :checkInDate AND :checkOutDate
            And (i.totalCount-i.bookCount-i.reservedCount)>=:numberOfRooms
            AND i.closed=false
            """)
    void initBooking(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("numberOfRooms") int numberOfRooms
    );
}
