package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity,Long> {
}
