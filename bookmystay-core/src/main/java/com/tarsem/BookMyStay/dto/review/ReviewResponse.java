package com.tarsem.BookMyStay.dto.review;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {

    private Long reviewId;

    private String guestName;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;

}
