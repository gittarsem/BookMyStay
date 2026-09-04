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
public class AdminReviewDTO {

    private Long reviewId;

    private String guestName;

    private Long guestId;

    private Long hotelId;

    private String hotelName;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;
}