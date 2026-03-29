package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.InventoryEntity;
import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.dto.InventoryDTO;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InventoryRepo extends JpaRepository<InventoryEntity,Long> {
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
}
