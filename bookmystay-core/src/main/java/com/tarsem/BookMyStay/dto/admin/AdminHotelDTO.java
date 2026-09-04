package com.tarsem.BookMyStay.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminHotelDTO {

    private Long id;

    private String name;

    private String city;

    private boolean active;

    private Double minPrice;

    private Double averageRating;

    private Integer totalReviews;

    private Long ownerId;

    private String ownerName;

    private String ownerEmail;

    private LocalDateTime createdAt;
}