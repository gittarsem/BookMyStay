package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.ReportEntity;
import com.tarsem.BookMyStay.Enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository
        extends JpaRepository<ReportEntity, Long> {

    Page<ReportEntity> findByStatus(
            ReportStatus status,
            Pageable pageable
    );

    long countByStatus(ReportStatus status);
}