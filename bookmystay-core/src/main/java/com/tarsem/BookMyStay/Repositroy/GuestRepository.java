package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.GuestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestRepository extends JpaRepository<GuestEntity, Long> {
}
