package com.tarsem.BookMyStay.dto.profile;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class DeleteAccountRequestDTO {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "Confirmation is required")
    private String confirmation;
}
