package com.tarsem.BookMyStay.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateReviewRequest {

    @NotNull
    private long bookingId;

    @NotNull
    @Min(1)
    @Max(5)
    private int rating;

    @NotBlank
    private String comment;
}
