package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.BookingEntity;
import com.tarsem.BookMyStay.Enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity,Long> {

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
}
