package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.RoomTypePricingEntity;
import com.tarsem.BookMyStay.Enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoomTypePricingRepository extends JpaRepository<RoomTypePricingEntity,Long> {

    Optional<RoomTypePricingEntity> findByHotelIdAndRoomType(
            Long hotelId, RoomType roomType
    );

    Optional<RoomTypePricingEntity> findByIdAndHotelId(Long roomId, Long hotelId);
}
