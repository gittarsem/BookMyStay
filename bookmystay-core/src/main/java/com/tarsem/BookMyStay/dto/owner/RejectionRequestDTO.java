package com.tarsem.BookMyStay.dto.owner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectionRequestDTO {

    @NotBlank(message = "Rejection reason is required")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;
}