package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity,Long> {

    @Query("SELECT MIN(r.Price) FROM RoomEntity r WHERE r.hotel.id = :hotelId")
    BigDecimal findMinPriceByHotelId(Long hotelId);
}
