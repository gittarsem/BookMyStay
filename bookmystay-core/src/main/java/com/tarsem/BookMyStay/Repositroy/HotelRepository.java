package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HotelRepository
        extends JpaRepository<HotelEntity, Long>,
        JpaSpecificationExecutor<HotelEntity> {

    List<HotelEntity> findByOwner(UserEntity user);

    List<HotelEntity> findAllByOwner(UserEntity owner);

    List<HotelEntity> findByIdIn(List<Long> ids);

    boolean existsByName(String name);

    Optional<HotelEntity> findByName(String hotelName);

    long countByActiveTrue();

    long countByActiveFalse();

    @Query(value = """
            SELECT h.*
            FROM hotels h
            WHERE h.active = true

              AND (
                    :keyword IS NULL
                    OR TRIM(:keyword) = ''
                    OR LOWER(h.name) LIKE LOWER(CONCAT('%', TRIM(:keyword), '%'))
                    OR LOWER(h.location) LIKE LOWER(CONCAT('%', TRIM(:keyword), '%'))
              )

              AND (
                    :city IS NULL
                    OR TRIM(:city) = ''
                    OR LOWER(h.location) LIKE LOWER(CONCAT('%', TRIM(:city), '%'))
              )

              AND (
                    :minPrice IS NULL
                    OR h.min_price >= :minPrice
              )

              AND (
                    :maxPrice IS NULL
                    OR h.min_price <= :maxPrice
              )

              AND (
                    :ratings IS NULL
                    OR h.average_rating >= :ratings
              )

            ORDER BY
                CASE
                    WHEN :sortField = 'price'
                     AND :sortOrder = 'asc'
                    THEN h.min_price
                END ASC,

                CASE
                    WHEN :sortField = 'price'
                     AND :sortOrder = 'desc'
                    THEN h.min_price
                END DESC,

                CASE
                    WHEN :sortField = 'ratings'
                     AND :sortOrder = 'asc'
                    THEN h.average_rating
                END ASC,

                CASE
                    WHEN :sortField = 'ratings'
                     AND :sortOrder = 'desc'
                    THEN h.average_rating
                END DESC,

                CASE
                    WHEN :sortField = 'name'
                     AND :sortOrder = 'asc'
                    THEN LOWER(h.name)
                END ASC,

                CASE
                    WHEN :sortField = 'name'
                     AND :sortOrder = 'desc'
                    THEN LOWER(h.name)
                END DESC,

                h.id ASC
            """,
            nativeQuery = true)
    List<HotelEntity> searchHotels(
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("ratings") Double ratings,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder
    );
}