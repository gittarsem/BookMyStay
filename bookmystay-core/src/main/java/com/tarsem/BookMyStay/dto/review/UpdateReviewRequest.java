package com.tarsem.BookMyStay.dto.review;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReviewRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private int ratings;

    @NotBlank
    @Size(max = 1000)
    private String comment;
}
