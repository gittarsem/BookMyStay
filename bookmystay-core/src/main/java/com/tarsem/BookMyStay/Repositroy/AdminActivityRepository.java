package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.AdminActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminActivityRepository
        extends JpaRepository<AdminActivityEntity, Long> {
}