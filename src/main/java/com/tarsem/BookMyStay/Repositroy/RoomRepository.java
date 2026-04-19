package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity,Long> {
}
