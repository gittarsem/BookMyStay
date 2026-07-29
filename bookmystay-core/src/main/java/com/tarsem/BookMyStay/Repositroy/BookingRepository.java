package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.BookingEntity;
import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
