package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRepository extends JpaRepository<HotelEntity, Long> {

    List<HotelEntity> findByOwner(UserEntity user);
    List<HotelEntity> findAllByOwner(UserEntity owner);
}
