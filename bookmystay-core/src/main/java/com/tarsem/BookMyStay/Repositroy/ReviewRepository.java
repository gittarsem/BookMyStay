package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.ReviewEntity;
import com.tarsem.BookMyStay.dto.review.ReviewStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity,Long> {

    @Query("""
    SELECT
        AVG(r.rating) AS averageRating,
        COUNT(r) AS totalReviews
    FROM ReviewEntity r
    WHERE r.hotel.id = :hotelId
""")
    ReviewStatistics calculateHotelStatistics(@Param("hotelId") Long hotelId);

    Page<ReviewEntity> findByHotelId(Long hotelId, Pageable pageable);

    Page<ReviewEntity> findByRating(Integer rating, Pageable pageable);
}
